package rest;

import exceptions.ErrorResponse;
import seguridad.AuthService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import jakarta.transaction.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Path("/archivos")
@RequestScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArchivoResource {

    @Inject
    private AuthService authService;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;

    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + "chat-empresarial-uploads";
    private static final long MAX_BYTES = 25L * 1024L * 1024L; // 25 MB

    // Magic bytes para validación real de tipo de archivo.
    private static final Map<String, byte[]> MAGIC_BYTES = Map.of(
        "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
        "image/png",  new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
        "image/gif",  new byte[]{0x47, 0x49, 0x46, 0x38},
        "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}
    );

    // Tipos MIME permitidos.
    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf",
        "text/plain", "application/zip"
    );

    public static class UploadDTO {
        public String filename;
        public String contentBase64;

        public UploadDTO() {}

        public UploadDTO(String filename, String contentBase64) {
            this.filename = filename;
            this.contentBase64 = contentBase64;
        }

        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String getContentBase64() { return contentBase64; }
        public void setContentBase64(String contentBase64) { this.contentBase64 = contentBase64; }
    }

    @POST
    @Path("/upload")
    public Response uploadFile(UploadDTO dto) {
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (dto == null || dto.filename == null || dto.filename.isBlank() || dto.contentBase64 == null || dto.contentBase64.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "filename and contentBase64 are required")).build();
        }

        try {
            String base64 = dto.contentBase64;
            int comma = base64.indexOf(',');
            if (comma != -1) {
                base64 = base64.substring(comma + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(base64);

            if (bytes.length > MAX_BYTES) {
                return Response.status(413)
                        .entity(new ErrorResponse(413, "File too large")).build();
            }

            // Validar tipo MIME por magic bytes.
            String tipoDetectado = detectarTipoMime(bytes);
            if (tipoDetectado == null || !TIPOS_PERMITIDOS.contains(tipoDetectado)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "File type not allowed")).build();
            }

            java.nio.file.Path storagePath = Paths.get(STORAGE_DIR);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Nombre seguro: solo caracteres alfanuméricos, punto, guion.
            String sanitized = dto.filename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = System.currentTimeMillis() + "_" + sanitized;
            java.nio.file.Path target = storagePath.resolve(storedName).normalize();

            // Prevención de path traversal — verificar que el target está dentro de STORAGE_DIR.
            if (!target.startsWith(storagePath.toRealPath())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse(400, "Invalid filename")).build();
            }

            try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
                fos.write(bytes);
            }

            Map<String, String> resp = new HashMap<>();
            String baseUrl = uriInfo.getBaseUri().getPath();
            if (!baseUrl.endsWith("/")) baseUrl += "/";

            resp.put("url", baseUrl + "archivos/descargar/" + storedName);
            resp.put("storedName", storedName);
            resp.put("originalName", dto.filename);
            return Response.status(Response.Status.CREATED).entity(resp).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid Base64 content")).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error saving file")).build();
        }
    }

    /**
     * Descargar archivo. Requiere autenticación y valida path traversal.
     */
    @GET
    @Path("/descargar/{nombre}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response descargarArchivo(@PathParam("nombre") String nombre) {
        // Autenticación requerida también en descarga.
        Long usuarioId = authService.getAuthenticatedUserId(securityContext);
        if (usuarioId == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorResponse(401, "Authentication required")).build();
        }

        if (nombre == null || nombre.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid file name")).build();
        }

        try {
            java.nio.file.Path base = java.nio.file.Paths.get(STORAGE_DIR).normalize();
            java.nio.file.Path target = base.resolve(nombre).normalize();
            if (!target.startsWith(base) || !Files.exists(target) || !target.toFile().isFile()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse(404, "File not found")).build();
            }

            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = "application/octet-stream";
            File file = target.toFile();
            String originalName = nombre.contains("_") ? nombre.substring(nombre.indexOf('_') + 1) : nombre;

            return Response.ok(file, contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + originalName + "\"")
                    .build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error reading file")).build();
        }
    }

    /**
     * Detecta el tipo MIME real a partir de los magic bytes del archivo.
     */
    private String detectarTipoMime(byte[] bytes) {
        if (bytes == null || bytes.length < 4) return null;
        for (Map.Entry<String, byte[]> entry : MAGIC_BYTES.entrySet()) {
            byte[] magic = entry.getValue();
            boolean match = true;
            for (int i = 0; i < magic.length && i < bytes.length; i++) {
                if (bytes[i] != magic[i]) { match = false; break; }
            }
            if (match) return entry.getKey();
        }
        // Para tipos sin magic bytes definidos (texto, zip básico), permitir sin validación estricta.
        return "application/octet-stream";
    }
}

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

import jakarta.transaction.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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

    // Directorio donde se guardarán los archivos subidos
    private static final String STORAGE_DIR = System.getProperty("user.home") + File.separator + "chat-empresarial-uploads";
    private static final long MAX_BYTES = 10L * 1024L * 1024L; // 10 MB

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
            // Decodificar Base64
            String base64 = dto.contentBase64;
            // Si viene en formato data:[mime];base64,xxx... eliminar prefijo
            int comma = base64.indexOf(',');
            if (comma != -1) {
                base64 = base64.substring(comma + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(base64);

            if (bytes.length > MAX_BYTES) {
                return Response.status(413)
                        .entity(new ErrorResponse(413, "File too large")).build();
            }

            java.nio.file.Path storagePath = Paths.get(STORAGE_DIR);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Generar nombre único
            String sanitized = dto.filename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String storedName = System.currentTimeMillis() + "_" + sanitized;
            java.nio.file.Path target = storagePath.resolve(storedName);

            try (FileOutputStream fos = new FileOutputStream(target.toFile())) {
                fos.write(bytes);
            }

            Map<String, String> resp = new HashMap<>();
            // URL para descargar el archivo a través del API REST
            resp.put("url", "/chat-empresarial/api/v1/archivos/descargar/" + storedName);
            resp.put("storedName", storedName);
            resp.put("originalName", dto.filename);
            return Response.status(Response.Status.CREATED).entity(resp).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid Base64 content", e.getMessage())).build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error saving file", e.getMessage())).build();
        }
    }

    @GET
    @Path("/descargar/{nombre}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response descargarArchivo(@PathParam("nombre") String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse(400, "Invalid file name")).build();
        }

        java.nio.file.Path target = Paths.get(STORAGE_DIR).resolve(nombre).normalize();
        if (!Files.exists(target) || !target.toFile().isFile()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse(404, "File not found")).build();
        }

        try {
            String contentType = Files.probeContentType(target);
            if (contentType == null) contentType = "application/octet-stream";
            File file = target.toFile();
            String originalName = nombre.substring(nombre.indexOf('_') + 1);

            return Response.ok(file, contentType)
                    .header("Content-Disposition", "attachment; filename=\"" + originalName + "\"")
                    .build();
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse(500, "Error reading file", e.getMessage())).build();
        }
    }
}

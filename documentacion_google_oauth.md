# Cómo solucioné el problema con Google OAuth 2.0 y configuré las credenciales en mi Chat Java EE

Este documento detalla el problema que tuve hoy al mezclar las credenciales de mi chat de Java EE con las de otra aplicación (Agenda PHP), y cómo lo solucioné configurando el Client ID correcto tanto en el frontend como en el backend.

---

## 1. El Problema: Error de OAuth por usar el ID de cliente equivocado

Al intentar iniciar sesión con Google en el entorno de la máquina virtual (producción), la aplicación fallaba lanzando un error de autenticación. 

**Causa del error:**
Tenía configurado el Client ID de otra aplicación mía (`Agenda PHP`), en lugar del ID correspondiente a este proyecto de chat. Esto provocaba que los orígenes autorizados no coincidieran en la consola de Google Developer y la autenticación fuese rechazada.

El ID de cliente **correcto** que debí configurar para esta aplicación es:
`623580687397-vd3dbk2u500dsda1tiact908fc4lq9s7.apps.googleusercontent.com`

---

## 2. Paso 1: Configurar correctamente mi Google Cloud Console

Para corregir esto, me aseguré de tener los siguientes parámetros configurados en mi cuenta de la [Google Cloud Console](https://console.cloud.google.com/):

1. **Pantalla de Consentimiento de OAuth:**
   * Configurada en tipo **Externo**.
   * Con los permisos (scopes) básicos para leer el perfil: `.../auth/userinfo.profile` y `.../auth/userinfo.email`.

2. **ID de cliente de OAuth en Credenciales:**
   * **Tipo de aplicación:** Aplicación web.
   * **Orígenes de JavaScript autorizados:**
     * Para mis pruebas locales: `http://localhost:5173`
     * Para mi entorno en producción: `https://java-ee-chat.duckdns.org`
   * **URIs de redireccionamiento autorizados:** Se dejan vacíos ya que el login lo realiza directamente el script del frontend (pop-up) y no requiere que el backend intercepte la redirección.

---

## 3. Paso 2: Corrección en el Frontend (Vue 3 / Vuetify)

Fui a mi archivo frontend `LoginVista.vue` y corregí el inicializador de la API de Google para que use mi Client ID real del chat en lugar del de la otra app:

```javascript
/* global google */
onMounted(() => {
  // Inicializo con el ID de cliente correcto para Java EE
  google.accounts.id.initialize({
    client_id: "623580687397-vd3dbk2u500dsda1tiact908fc4lq9s7.apps.googleusercontent.com",
    callback: manejarLoginConGoogle
  });

  // Renderizo el botón oficial de Google
  google.accounts.id.renderButton(
    document.getElementById("botonGoogle"),
    { theme: "outline", size: "large", width: "100%" }
  );
});
```

Al hacer clic y autenticarme en Google, el frontend recibe la credencial firmada y se la envía a mi backend en formato POST.

---

## 4. Paso 3: Corrección en el Backend (Java / Jakarta EE)

Como el servidor no debe confiar en lo que el cliente le envía, el backend tiene que descifrar y verificar criptográficamente la credencial que le envía mi frontend.

Fui al archivo `UsuarioResource.java` en mi capa de servicios y actualicé la constante del Client ID para validar el token JWT recibido de Google de manera segura:

```java
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

// ...

// Mi ID de cliente correcto
String clientID = "623580687397-vd3dbk2u500dsda1tiact908fc4lq9s7.apps.googleusercontent.com";

GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
        new NetHttpTransport(), 
        new GsonFactory()
    )
    .setAudience(Collections.singletonList(clientID))
    .build();

// googleToken es el token JWT que el frontend le mandó a mi API
GoogleIdToken idToken = verifier.verify(googleToken);

if (idToken != null) {
    GoogleIdToken.Payload payload = idToken.getPayload();

    // Extraigo la info del usuario verificada por Google
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    String pictureUrl = (String) payload.get("picture");

    // Lógica de mi sistema:
    // 1. Busco si este email ya existe en mi base de datos de chat.
    // 2. Si es la primera vez que entra, le creo una cuenta local usando su nombre y avatar de Google.
    // 3. Genero mi propio token de sesión (JWT) y se lo devuelvo al frontend.
} else {
    throw new SecurityException("El token de Google no es válido");
}
```

---

## 5. Resumen del aprendizaje

El error de hoy me enseñó que:
1. **Los IDs de cliente son únicos por proyecto:** Mezclar credenciales entre diferentes aplicaciones (como me pasó con Agenda PHP y este Chat Java EE) rompe la cadena de confianza de OAuth 2.0.
2. **Ambas partes deben coincidir:** Si cambias el ID en el frontend, debes actualizarlo inmediatamente en el backend, ya que la librería `GoogleIdTokenVerifier` de Java valida que el parámetro `aud` (audiencia) del token sea idéntico al ID del cliente registrado, bloqueando la solicitud en caso de discrepancias.

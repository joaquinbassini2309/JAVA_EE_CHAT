document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');

    registerForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const username = document.getElementById('username').value;
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('/chat-empresarial/api/v1/usuarios/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, email, password }),
            });

            if (response.status === 201) { // 201 Created
                alert('¡Registro exitoso! Ahora puedes iniciar sesión.');
                window.location.href = 'InicioSesion.html';
            } else {
                const errorData = await response.json();
                // El mensaje de error viene del backend, por ejemplo: "El correo electrónico ya se encuentra registrado"
                alert(`Error en el registro: ${errorData.message}`);
            }
        } catch (error) {
            console.error('Error al registrar usuario:', error);
            alert('Ocurrió un error de red. Por favor, inténtalo de nuevo.');
        }
    });
});
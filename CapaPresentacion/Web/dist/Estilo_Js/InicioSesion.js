document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        try {
            const response = await fetch('/chat-empresarial/api/v1/usuarios/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });

            if (response.ok) {
                const data = await response.json();
                // Guardar el token y los datos del usuario en localStorage
                localStorage.setItem('jwt', data.token);
                localStorage.setItem('usuario', JSON.stringify(data.usuario));
                
                // Redirigir a la vista principal
                window.location.href = 'VistaPrincipal.html';
            } else {
                const errorData = await response.json();
                alert(`Error: ${errorData.message}`);
            }
        } catch (error) {
            console.error('Error al iniciar sesión:', error);
            alert('Ocurrió un error al intentar iniciar sesión. Por favor, inténtalo de nuevo.');
        }
    });
});
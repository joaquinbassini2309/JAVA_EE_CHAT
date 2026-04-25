document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');

    loginForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;

        // Aquí se implementará la lógica de autenticación
        console.log('Email:', email);
        console.log('Password:', password);

        // Simulación de inicio de sesión exitoso
        alert('Inicio de sesión exitoso (simulación)');
        // window.location.href = 'VistaPrincipal.html';
    });
});
package rest;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter("/*")
public class SpaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // Si la ruta es para la API, WebSocket, o es un archivo con extensión (js, css, img), dejar pasar.
        if (path.startsWith("/api") || path.startsWith("/ws") || path.matches(".*\\.[a-zA-Z0-9]+$")) {
            chain.doFilter(request, response);
        } else {
            // Para el resto (ej. /login, /chat), reescribir internamente a /index.html para que el Vue Router se encargue
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    @Override
    public void destroy() {}
}

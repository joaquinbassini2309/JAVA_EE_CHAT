package seguridad;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(urlPatterns = "/*")
public class SpaFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());

        if (path.startsWith("/api") || path.startsWith("/ws") || path.startsWith("/assets") || path.contains(".")) {
            chain.doFilter(request, response);
        } else {
            request.getRequestDispatcher("/index.html").forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}

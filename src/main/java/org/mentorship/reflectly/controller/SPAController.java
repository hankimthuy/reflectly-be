package org.mentorship.reflectly.controller;

import org.mentorship.reflectly.constants.RouteConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SPA Controller to handle all non-API routes
 * Ensures page refresh on any route serves index.html for React Router
 */
@Controller
public class SPAController {

    /**
     * Catch-all mapping for all non-API routes
     * Forwards to index.html so React Router can handle client-side routing
     * 
     * This solves the SPA routing problem where:
     * - User navigates to /profile → React Router handles it
     * - User refreshes /profile → Spring Boot looks for /profile file → Not found
     * - This controller catches it → Forwards to /index.html → React Router loads
     */
    @RequestMapping(value = {
        RouteConstants.ROOT,
        RouteConstants.SPA_CATCH_ALL
    })
    public String forward() {
        return "forward:/index.html";
    }
}

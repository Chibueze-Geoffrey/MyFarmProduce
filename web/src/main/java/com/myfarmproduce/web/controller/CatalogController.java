package com.myfarmproduce.web.controller;

import com.myfarmproduce.application.service.CatalogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/catalog")
public class CatalogController {

    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping
    public String index(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("categories", catalog.getCategoriesWithProducts(q));
        model.addAttribute("search", q);
        return "catalog/index";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Integer id, Model model, HttpServletResponse response) {
        var product = catalog.getProductById(id);
        if (product.isEmpty()) {
            response.setStatus(404);
            return "error/404";
        }
        model.addAttribute("product", product.get());
        return "catalog/details";
    }
}

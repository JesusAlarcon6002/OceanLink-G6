package com.oceanlink.servlets;

import com.oceanlink.beans.Segmentos;
import com.oceanlink.daos.SegmentosDao;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;

@WebServlet(name = "SegmentosServlet", value = "/SegmentosServlet")

public class SegmentosServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SegmentosDao SegmentosDao = new SegmentosDao();
        ArrayList<Segmentos> listaSegmentos = SegmentosDao.obtenerListaSegmentos();
        request.setAttribute("listaSegmentos", listaSegmentos);
        RequestDispatcher view = request.getRequestDispatcher("listaSegmentos.jsp");
        view.forward(request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}

package controller;

import conexion.Conexion;
import static controller.VENTA_REGISTRAR_PRODUCTO_CONTROLLER.ERROR_CODIGO_REPETIDO;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import modelo.PRODUCTO;
import modelo.USUARIO;
import org.json.JSONException;
import util.SIS_EVENTOS;

@WebServlet(name = "PUNTO_VENTA_CONTROLLER", urlPatterns = {"/PUNTO_VENTA_CONTROLLER"})
public class PUNTO_VENTA_CONTROLLER extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain");
        USUARIO usuario = ((USUARIO) request.getSession().getAttribute("usr"));
        if (usuario == null) {
            response.getWriter().write("false");
            return;
        }
        Conexion con = usuario.getCon();
        if (con == null) {
            response.getWriter().write("false");
            return;
        }
        con.Transacction();
        try {
            String html = "";
            String evento = request.getParameter("evento");
            switch (evento) {
                case "init":
                    html = init(request, con);
                    break;
                case "guardar_producto":
                    html = guardar_producto(request, con);
                    break;
                case "eliminar_producto":
                    html = eliminar_producto(request, con);
                    break;
            }
            con.commit();
            response.getWriter().write(html);
        } catch (SQLException ex) {
            con.error(this, ex);
            response.getWriter().write("false");
        } catch (JSONException ex) {
            con.error(this, ex);
            response.getWriter().write("false");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String init(HttpServletRequest request, Conexion con) throws SQLException, JSONException {
        return new PRODUCTO(con).todos().toString();
    }

    private String guardar_producto(HttpServletRequest request, Conexion con) throws SQLException, JSONException, IOException, ServletException {
//        int id = Integer.parseInt(request.getParameter("id"));
//        String codigo = request.getParameter("codigo");
//        String descripcion = request.getParameter("descripcion");
//        double precio_compra = Double.parseDouble(request.getParameter("precio_compra"));
//        double precio_venta = Double.parseDouble(request.getParameter("precio_venta"));
//        String imagen = null;
//        String ruta = null;
//        Part file = request.getPart("imagen");
//        SIS_EVENTOS ev = new SIS_EVENTOS();
//        boolean ok_subir = false;
//        String ruta_folder = this.getServletContext().getRealPath("/img") + File.separator + "productos" + File.separator;
//        String ruta_web = this.getServletContext().getContextPath() + "/img/productos/";
//        Date d = new Date();
//        if (id > 0) {
//            if (file != null && file.getSize() > 0 && file.getContentType().contains("image")) {
//                String tipo[] = file.getContentType().split("/");
//                if (tipo.length > 1) {
//                    imagen = ruta_web + "producto_" + id + "_" + d.getTime() + "." + tipo[1];
//                    ruta = ruta_folder + "producto_" + id + "_" + d.getTime() + "." + tipo[1];
//                } else {
//                    imagen = ruta_web + "producto_" + id + "_" + d.getTime() + "_" + file.getSubmittedFileName();
//                    ruta = ruta_folder + "producto_" + id + "_" + d.getTime() + "_" + file.getSubmittedFileName();
//                }
//                ok_subir = ev.guardarImagenEnElSistemaDeFicheros(file.getInputStream(), ruta);
//            }
//            PRODUCTO p = new PRODUCTO(con).buscar(id);
//            if (p == null) {
//                return "false";
//            }
//            if (!ok_subir) {
//                imagen = p.getIMAGEN();
//            }
//            String antImagen = p.getIMAGEN();
//            p.setCODIGO(codigo);
//            p.setNOMBRE(descripcion);
//            p.setIMAGEN(imagen);
//            try {
//                p.update();
//            } catch (Exception e) {
//                ev.eliminarImagenEnElSistemaDeFicheros(ruta);
//                if (e.getMessage().contains("uq_producto_codigo")) {
//                    return ERROR_CODIGO_REPETIDO;
//                } else {
//                    throw e;
//                }
//            }
//
//            if (antImagen != null && ok_subir) {
//                String ims[] = antImagen.split("/");
//                if (ims.length > 0) {
//                    ev.eliminarImagenEnElSistemaDeFicheros(ruta_folder + ims[ims.length - 1]);
//                }
//            }
//            return p.toJSONObject().toString();
//        } else {
//            PRODUCTO p = new PRODUCTO(id, codigo, descripcion, null, 0);
//            p.setCon(con);
//            try {
//                id = p.insert();
//            } catch (Exception e) {
//                if (e.getMessage().contains("uq_producto_codigo")) {
//                    return ERROR_CODIGO_REPETIDO;
//                } else {
//                    throw e;
//                }
//            }
//            if (file != null && file.getSize() > 0 && file.getContentType().contains("image")) {
//                String tipo[] = file.getContentType().split("/");
//                if (tipo.length > 1) {
//                    imagen = ruta_web + "producto_" + id + "_" + d.getTime() + "." + tipo[1];
//                    ruta = ruta_folder + "producto_" + id + "_" + d.getTime() + "." + tipo[1];
//                } else {
//                    imagen = ruta_web + "producto_" + id + "_" + d.getTime() + "_" + file.getSubmittedFileName();
//                    ruta = ruta_folder + "producto_" + id + "_" + d.getTime() + "_" + file.getSubmittedFileName();
//                }
//                ok_subir = ev.guardarImagenEnElSistemaDeFicheros(file.getInputStream(), ruta);
//            }
//            if (ok_subir) {
//                p.setIMAGEN(imagen);
//                p.setID(id);
//                p.update();
//            }
//            return p.toJSONObject().toString();
//        }
        return "";
    }

    private String eliminar_producto(HttpServletRequest request, Conexion con) throws SQLException, JSONException {
        int id = Integer.parseInt(request.getParameter("id"));
        PRODUCTO p = new PRODUCTO(con);
        p.setID(id);
        p.delete();
        return "true";
    }

}

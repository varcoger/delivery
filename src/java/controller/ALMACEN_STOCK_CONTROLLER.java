package controller;

import conexion.Conexion;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import modelo.DETALLE_NOTA_RECEPCION;
import modelo.INSUMO;
import modelo.INSUMO_GRUPO;
import modelo.INSUMO_GRUPO_DETALLE;
import modelo.NOTA_RECEPCION;
import modelo.USUARIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author benja
 */
@WebServlet(name = "ALMACEN_STOCK_CONTROLLER", urlPatterns = {"/ALMACEN_STOCK_CONTROLLER"})
public class ALMACEN_STOCK_CONTROLLER extends HttpServlet {

    private int id_sucursal = 1;

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
                case "todos":
                    html = todos(request, con);
                    break;
                case "todos_insumos":
                    html = todos_insumos(request, con);
                    break;
                case "aumentar_stock":
                    html = aumentar_stock(request, con);
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

    private String todos(HttpServletRequest request, Conexion con) throws SQLException, JSONException {
        int estado = Integer.parseInt(request.getParameter("estado"));
        if (estado == 0) {
            return new INSUMO(con).todos_Almacen(id_sucursal).toString();
        } else {
            return new INSUMO(con).stock_Almacen(id_sucursal).toString();
        }
    }

    private String todos_insumos(HttpServletRequest request, Conexion con) throws SQLException, JSONException {
        JSONObject json = new JSONObject();
        json.put("INSUMO", new INSUMO(con).todosConUM());
        json.put("INSUMO_GRUPO", new INSUMO_GRUPO(con).todos());
        return json.toString();
    }

    private String aumentar_stock(HttpServletRequest request, Conexion con) throws SQLException, JSONException {
        int lista_size = Integer.parseInt(request.getParameter("lista_size"));
        NOTA_RECEPCION nr = new NOTA_RECEPCION(0, 0, new Date(), id_sucursal, con.getUsuario().getID(), con.getUsuario().getID(), 0);
        nr.setCon(con);
        int id_nota = nr.insert();
        int id_insumo, cantidad;
        boolean grupo;
        double precio;
        DETALLE_NOTA_RECEPCION dnr = new DETALLE_NOTA_RECEPCION(con);
        for (int i = 0; i < lista_size; i++) {
            id_insumo = Integer.parseInt(request.getParameter("productos[" + i + "][id]"));
            cantidad = Integer.parseInt(request.getParameter("productos[" + i + "][cantidad]"));
            precio = Double.parseDouble(request.getParameter("productos[" + i + "][cantidad]"));
            grupo = Boolean.parseBoolean(request.getParameter("productos[" + i + "][grupo]"));
            if (grupo) {
                ArrayList<INSUMO_GRUPO_DETALLE> lista = new INSUMO_GRUPO_DETALLE(con).todosXID_INSUMO_GRUPO(id_insumo);
                double total_cant = .0;
                for (int j = 0; j < lista.size(); j++) {
                    total_cant += lista.get(j).getCANTIDAD();
                }
                double total_pre = .0;
                double parcial_pre;
                for (int j = 0; j < lista.size(); j++) {
                    INSUMO_GRUPO_DETALLE igd = lista.get(j);
                    dnr.setCANTIDAD(cantidad * igd.getCANTIDAD());
                    dnr.setID_NOTA_RECEPCION(id_nota);
                    dnr.setID_INSUMO(igd.getID_INSUMO());
                    dnr.setID_INSUMO_GRUPO(igd.getID_INSUMO_GRUPO());
                    if (j == lista.size() - 1) {
                        parcial_pre = precio - total_pre;
                    } else {
                        parcial_pre = igd.getCANTIDAD() / total_cant * precio;
                        total_pre += parcial_pre;
                    }
                    dnr.setPRECIO(parcial_pre);
                    dnr.setID(0);
                    dnr.insert();
                }
            } else {
                dnr.setCANTIDAD(cantidad);
                dnr.setID_NOTA_RECEPCION(id_nota);
                dnr.setID_INSUMO(id_insumo);
                dnr.setID_INSUMO_GRUPO(0);
                dnr.setPRECIO(precio);
                dnr.setID(0);
                dnr.insert();
            }
        }
        return nr.notaRececpcionPDF(id_nota).toString();
    }
}

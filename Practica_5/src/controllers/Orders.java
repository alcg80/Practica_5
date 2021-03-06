package controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import com.sun.xml.internal.messaging.saaj.packaging.mime.internet.MimeMultipart;

import model.Logic;
import model.beans.PurchaseLineBean;
import model.beans.PedidoBean;
import model.beans.ProductoBean;
import model.beans.UserBean;

/**
 * Servlet implementation class Orders
 */
@WebServlet("/Orders")
public class Orders extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAILUSER = "marisma2014@yahoo.es";
	private static final String MAILPASS = "Pepito1234";
	private Logic logic;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Orders() {
		super();
		logic = new Logic();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		HttpSession session = request.getSession();

		if (request.getParameter("addCesta") != null) {
			ArrayList<ProductoBean> cesta;
			int id = Integer.parseInt(request.getParameter("prod"));
			ProductoBean producto = logic.setProducto(id,
					Integer.parseInt(request.getParameter("cant")));

			if (session.getAttribute("cesta") != null) {
				cesta = (ArrayList<ProductoBean>) session.getAttribute("cesta");
				cesta.add(producto);
				session.setAttribute("cesta", cesta);
			}
			else {
				cesta = new ArrayList<ProductoBean>();
				cesta.add(producto);
				session.setAttribute("cesta", cesta);
			}

			String page = request.getParameter("page");
			page += (page.equals("index.jsp")) ? "?cesta=" + cesta.size()
					: "&cesta=" + cesta.size();
			response.sendRedirect(page);
		}

		if (request.getParameter("viewCart") != null) {
			if (session.getAttribute("cesta") != null) {
				ArrayList<ProductoBean> cesta = (ArrayList<ProductoBean>) session
						.getAttribute("cesta");
				session.setAttribute(
						"pedido",
						getPedidoFromCesta(cesta,
								(UserBean) session.getAttribute("isLogin")));
				response.sendRedirect("views/viewCart.jsp?cesta="
						+ cesta.size());
			}
			else {
				String page = request.getParameter("page");
				page += (page.equals("index.jsp")) ? "?verCesta=false"
						: "&verCesta=false";
				response.sendRedirect(page);
			}
		}

		if (request.getParameter("pay") != null) {
			PedidoBean pedido = (PedidoBean) session.getAttribute("pedido");
			pedido.setUsuario((UserBean) getServletContext().getAttribute(
					"isLogin"));
			logic.insertPedido(pedido);
			// Send the mail to admin
			try {
				this.sendEmail("Compra realizada", session, "2dfs1011@gmail.com");
			}
			catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			session.removeAttribute("cesta");
			response.sendRedirect("views/payment.jsp");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private PedidoBean getPedidoFromCesta(ArrayList<ProductoBean> cesta,
			UserBean usuario) {
		ArrayList<PurchaseLineBean> lineasPedido = new ArrayList<PurchaseLineBean>();
		double importe = 0;

		int id = 1;
		for (ProductoBean producto : cesta) {
			PurchaseLineBean linea = new PurchaseLineBean();
			int index = estaDuplicado(producto, lineasPedido);

			if (index == -1) {
				linea.setId(id++);
				linea.setCantidad(producto.getCantidad());
				linea.setProducto(producto);
				lineasPedido.add(linea);
			}
			else {
				linea = lineasPedido.get(index);
				linea.setCantidad(linea.getCantidad() + producto.getCantidad());
				lineasPedido.set(index, linea);
			}
			importe += producto.getPrecio() * producto.getCantidad();
		}

		PedidoBean pedido = new PedidoBean();
		pedido.setUsuario(usuario);
		pedido.setImporte(importe);
		pedido.setLineasPedido(lineasPedido);

		return pedido;
	}

	/**
	 * Devuelve el indice (id - 1) del ArrayList<PurchaseLineBean> si el
	 * producto está duplicado en la cesta; si no, devuelve -1
	 */
	private int estaDuplicado(ProductoBean producto,
			ArrayList<PurchaseLineBean> lineasPedido) {
		for (int i = 0; i < lineasPedido.size(); i++) {
			if (lineasPedido.get(i).getProducto().getId() == producto.getId()) {
				return i;
			}
		}
		return -1;
	}

	private void sendEmail(String subject, HttpSession httpSession,
			String recipient) throws AddressException, MessagingException {
		Properties properties = new Properties();
		properties.setProperty("mail.smtp.host", "smtp.mail.yahoo.com");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.smtp.port", "587");
		properties.setProperty("mail.smtp.USER", MAILUSER);
		properties.setProperty("mail.smtp.auth", "true");

		Session session = Session.getDefaultInstance(properties);
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(MAILUSER));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(
				recipient));
		message.setSubject(subject);

		StringBuffer output = new StringBuffer();
		output.append("COMPRA REALIZADA\n\n");
		output.append("PRODUCTOS\n\n");
		PedidoBean pedido = (PedidoBean) httpSession.getAttribute("pedido");
		ArrayList<PurchaseLineBean> lineasPedido = pedido.getLineasPedido();
		 output.append("NOMBRE");
		 output.append("\t");
		 output.append("PRECIO");
		 output.append("\t");
		 output.append("CANTIDAD");
		 output.append("\t");
		 output.append("IMPORTE");
		 output.append("\n");
		 for (PurchaseLineBean purchaseLineBean : lineasPedido) {
		 output.append(purchaseLineBean.getProducto().getNombre());
		 output.append("\t");
		 output.append(purchaseLineBean.getProducto().getPrecio());
		 output.append("\t");
		 output.append(purchaseLineBean.getCantidad());
		 output.append("\t");
		 output.append(purchaseLineBean.getProducto().getPrecio()
		 * purchaseLineBean.getCantidad());
		 output.append("\n");
		 }
		 output.append("\n\n");
		 output.append("TOTAL = " + pedido.getImporte());

		message.setText(output.toString());
		Transport transport = session.getTransport("smtp");
		transport.connect(MAILUSER, MAILPASS);
		transport.sendMessage(message, message.getAllRecipients());
		transport.close();

	}
}

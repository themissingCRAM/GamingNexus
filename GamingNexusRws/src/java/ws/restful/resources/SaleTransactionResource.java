/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.restful.resources;

import ejb.session.stateless.CustomerSessionBeanLocal;
import ejb.session.stateless.ProductSessionBeanLocal;
import ejb.session.stateless.SaleTransactionSessionBeanLocal;
import entity.Customer;
import entity.Product;
import entity.SaleTransaction;
import entity.SaleTransactionLineItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import util.exception.CreateNewSaleTransactionException;
import util.exception.CustomerNotFoundException;
import util.exception.ProductNotFoundException;
import ws.restful.helperClass.ProductAndQuantity;
import ws.restful.model.CreateSaleTransactionReq;
import ws.restful.model.CreateSaleTransactionRsp;
import ws.restful.model.ErrorRsp;

/**
 * REST Web Service
 *
 * @author ufoya
 */
@Path("SaleTransaction")
public class SaleTransactionResource {

    CustomerSessionBeanLocal customerSessionBean = lookupCustomerSessionBeanLocal();

    SaleTransactionSessionBeanLocal saleTransactionSessionBean = lookupSaleTransactionSessionBeanLocal();

    ProductSessionBeanLocal productSessionBean = lookupProductSessionBeanLocal();

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of SaleTransactionResource
     */
    public SaleTransactionResource() {
    }

    /**
     * Retrieves representation of an instance of
     * ws.restful.resources.SaleTransactionResource
     *
     * @return an instance of java.lang.String
     */
    /**
     * PUT method for updating or creating an instance of
     * SaleTransactionResource
     *
     * @param content representation for the resource
     */
    @Path("retrieveAllSaleTransactionByUsernameAndPassword")
    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllSaleTransactionsByUsernameAndPassword(@QueryParam("username") String username,
            @QueryParam("password") String password) {
        try {
            List<SaleTransaction> saleTransactions = saleTransactionSessionBean.retrieveAllSaleTransactionByUsernameAndPassword(username, password);

            for (SaleTransaction saleTransaction : saleTransactions) {
                
                saleTransaction.getCustomer().getSaleTransactions().clear();
                
                for(SaleTransactionLineItem saleTransactionLineItem : saleTransaction.getSaleTransactionLineItems()) {
                    saleTransactionLineItem.setProduct(null);
                }
            }
            
            return Response.status(Response.Status.OK).entity(new RetrieveAllSaleTransactionByUsernameAndPasswordRsp(saleTransactions)).build();

        } catch (Exception ex) {
            ErrorRsp errorRsp = new ErrorRsp(ex.getMessage());

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorRsp).build();
        }
    }

    @Path("createSaleTransaction")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSaleTransation(CreateSaleTransactionReq createSaleTransactionReq) {

        System.out.println("********** createSaleTransation() is successfully invoked");
        //System.out.println("*****" + createSaleTransactionReq.getUsername());
        //System.out.println("*****" + createSaleTransactionReq.getPassword());

        if (createSaleTransactionReq != null) {
            List<SaleTransactionLineItem> saleTransactionLineItems = new ArrayList<>();

            try {
                Customer c = customerSessionBean.retrieveCustomerByUsername(createSaleTransactionReq.getUsername());
                Integer totalLineItem = new Integer(createSaleTransactionReq.getProductList().size());

                Integer totalQuantity = 0;
                BigDecimal totalAmount = BigDecimal.valueOf(0);

                for (ProductAndQuantity productAndQuantity : createSaleTransactionReq.getProductList()) {
                    //System.out.println("Product Id" + productAndQuantity.getProductId());
                    //System.out.println("Quantity" + productAndQuantity.getQuantity());      

                    Product product = productSessionBean.retrieveProductById(productAndQuantity.getProductId());
                    BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
                    BigDecimal subTotal = BigDecimal.valueOf(product.getPrice() * productAndQuantity.getQuantity());
                    System.out.println("Subtotal" + subTotal);
                    SaleTransactionLineItem newSaleTransactionLineItem = new SaleTransactionLineItem(product, productAndQuantity.getQuantity(), unitPrice, subTotal);
                    saleTransactionLineItems.add(newSaleTransactionLineItem);

                    totalQuantity += productAndQuantity.getQuantity();
                    totalAmount = totalAmount.add(subTotal);
                    System.out.println("Total Amount" + totalAmount);
                }
                SaleTransaction newSaleTransaction = new SaleTransaction(totalLineItem, totalQuantity, totalAmount, LocalDateTime.now(), saleTransactionLineItems, false);
                Long newSaleTransactionId = saleTransactionSessionBean.createNewSaleTransaction(c.getUserId(), newSaleTransaction);
                CreateSaleTransactionRsp createSaleTransactionRsp = new CreateSaleTransactionRsp(newSaleTransactionId);
                return Response.status(Response.Status.OK).entity(createSaleTransactionRsp).build();

            } catch (ProductNotFoundException | CustomerNotFoundException | CreateNewSaleTransactionException ex) {
                ErrorRsp errorRsp = new ErrorRsp(ex.getMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorRsp).build();
            }
        } else {
            ErrorRsp errorRsp = new ErrorRsp("Invalid request");
            return Response.status(Response.Status.BAD_REQUEST).entity(errorRsp).build();
        }
    }

    private ProductSessionBeanLocal lookupProductSessionBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (ProductSessionBeanLocal) c.lookup("java:global/GamingNexus/GamingNexus-ejb/ProductSessionBean!ejb.session.stateless.ProductSessionBeanLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private SaleTransactionSessionBeanLocal lookupSaleTransactionSessionBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (SaleTransactionSessionBeanLocal) c.lookup("java:global/GamingNexus/GamingNexus-ejb/SaleTransactionSessionBean!ejb.session.stateless.SaleTransactionSessionBeanLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }

    private CustomerSessionBeanLocal lookupCustomerSessionBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (CustomerSessionBeanLocal) c.lookup("java:global/GamingNexus/GamingNexus-ejb/CustomerSessionBean!ejb.session.stateless.CustomerSessionBeanLocal");
        } catch (NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "exception caught", ne);
            throw new RuntimeException(ne);
        }
    }
}

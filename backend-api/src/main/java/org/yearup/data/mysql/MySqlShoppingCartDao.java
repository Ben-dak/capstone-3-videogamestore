package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao
{
    private final ProductDao productDao;

    public MySqlShoppingCartDao(DataSource dataSource, ProductDao productDao)
    {
        super(dataSource);
        this.productDao = productDao;
    }

    //gets the cart
    @Override
    public ShoppingCart getByUserId(int userId)
    {
        ShoppingCart cart = new ShoppingCart();

        String sql =
                "SELECT product_id, quantity " +
                        "FROM shopping_cart " +
                        "WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery())
            {
                while (resultSet.next())
                {
                    int productId = resultSet.getInt("product_id");
                    int quantity = resultSet.getInt("quantity");

                    Product product = productDao.getById(productId);
                    ShoppingCartItem item =
                            new ShoppingCartItem(product, quantity);

                    cart.add(item);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return cart;
    }

    // adds product
    @Override
    public void addProduct(int userId, int productId)
    {
        String sql =
                "INSERT INTO shopping_cart (user_id, product_id, quantity) " +
                        "VALUES (?, ?, 1)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProductQuantity(int userId, int productId, int quantity)
    {
        String sql =
                "UPDATE shopping_cart " +
                        "SET quantity = ? " +
                        "WHERE user_id = ? AND product_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, userId);
            preparedStatement.setInt(3, productId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearCart(int userId)
    {
        String sql =
                "DELETE FROM shopping_cart " +
                        "WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql))
        {
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}


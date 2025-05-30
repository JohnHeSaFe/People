package model.dao;

import model.entity.Person;
import start.Routes;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * This class implements the IDAO interface and completes the function code
 * blocks so that they can operate with a SQL DDBB. The NIF is used as the
 * primary key.
 *
 * @author Francesc Perez
 * @version 1.1.0
 */
public class DAOSQL implements IDAO {

    private final String SQL_SELECT_ALL = "SELECT * FROM " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + ";";
    private final String SQL_SELECT = "SELECT * FROM " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + " WHERE (nif = ?);";
    private final String SQL_INSERT = "INSERT INTO " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + " (nif, name, dateOfBirth, photo, phone, postalCode, email) VALUES (?, ?, ?, ?, ?, ?, ?);";
    private final String SQL_UPDATE = "UPDATE " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + " SET name = ?, dateOfBirth = ?, photo = ? , phone = ? , postalCode = ? , email = ? WHERE (nif = ?);";
    private final String SQL_DELETE = "DELETE FROM " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + " WHERE (nif = ?)";
    private final String SQL_DELETE_ALL = "TRUNCATE " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE();
    private final String SQL_COUNT = "SELECT COUNT(*) AS total FROM " + Routes.DB.getDbServerDB() + "." + Routes.DB.getDbServerTABLE() + ";";


    public Connection connect() throws SQLException {
        Connection conn;
        conn = DriverManager.getConnection(Routes.DB.getDbServerAddress() + Routes.DB.getDbServerComOpt(), Routes.DB.getDbServerUser(), Routes.DB.getDbServerPassword());
        return conn;
    }

    public void disconnect(Connection conn) throws SQLException {
        conn.close();
    }

    @Override
    public Person read(Person p) throws SQLException {
        Person pReturn = null;
        Connection conn;
        PreparedStatement instruction;
        ResultSet rs;
        conn = connect();
        instruction = conn.prepareStatement(SQL_SELECT);
        instruction.setString(1, p.getNif());
        rs = instruction.executeQuery();
        while (rs.next()) {
            String nif = rs.getString("nif");
            String name = rs.getString("name");
            int phone = rs.getInt("phone");
            pReturn = new Person(name, nif, phone);
            Date date = rs.getDate("dateOfBirth");
            if (date != null) {
                pReturn.setDateOfBirth(date);
            }
            String photo = rs.getString("photo");
            if (photo != null) {
                pReturn.setPhoto(new ImageIcon(photo));
            }
            String postalCode = rs.getString("postalCode");
            if (postalCode != null){
                pReturn.setPostalCode(postalCode);
            }
            String email = rs.getString("email");
            if (email != null){
                pReturn.setEmail(email);
            }
        }
        rs.close();
        instruction.close();
        disconnect(conn);
        return pReturn;
    }

    @Override
    public ArrayList<Person> readAll() throws SQLException{
        ArrayList<Person> people = new ArrayList<>();
        Connection conn;
        Statement instruction;
        ResultSet rs;
        conn = connect();
        instruction = conn.createStatement();
        rs = instruction.executeQuery(SQL_SELECT_ALL);
        while (rs.next()) {
            String nif = rs.getString("nif");
            String name = rs.getString("name");
            Date date = rs.getDate("dateOfBirth");
            String photo = rs.getString("photo");
            int phone = rs.getInt("phone");
            String postalCode = rs.getString("postalCode");
            String email = rs.getString("email");
            if (photo != null) {
                people.add(new Person(nif, name, date, new ImageIcon(photo), phone, postalCode, email));
            } else {
                people.add(new Person(nif, name, date, null, phone, postalCode, email));
            }
        }
        rs.close();
        instruction.close();
        disconnect(conn);
        return people;
    }

    @Override
    public void delete(Person p) throws SQLException {
        Connection conn;
        PreparedStatement instruction;
        conn = connect();
        instruction = conn.prepareStatement(SQL_DELETE);
        instruction.setString(1,p.getNif());
        instruction.executeUpdate();
        instruction.close();
        disconnect(conn);
        File photoFile = new File(Routes.DB.getFolderPhotos() + File.separator + p.getNif()
                + ".png");
        photoFile.delete();
    }

    @Override
    public void insert(Person p) throws IOException, SQLException {
        Connection conn;
        PreparedStatement instruction;
        conn = connect();
        instruction = conn.prepareStatement(SQL_INSERT);
        instruction.setString(1, p.getNif());
        instruction.setString(2, p.getName());
        if (p.getDateOfBirth() != null) {
            instruction.setDate(3, new java.sql.Date((p.getDateOfBirth()).getTime()));
        } else {
            instruction.setDate(3, null);
        }
        if (p.getPhoto() != null) {
            String sep = File.separator;
            File folder = new File(Routes.DB.getFolderPhotos());
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String filePath = Routes.DB.getFolderPhotos() + sep + p.getNif() + ".png";
            File photo = new File(filePath);
            FileOutputStream out;
            BufferedOutputStream outB;
            out = new FileOutputStream(photo);
            outB = new BufferedOutputStream(out);
            BufferedImage bi = new BufferedImage(p.getPhoto().getImage().getWidth(null),
                    p.getPhoto().getImage().getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(p.getPhoto().getImage(), 0, 0, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] img = baos.toByteArray();
            for (int i = 0; i < img.length; i++) {
                outB.write(img[i]);
            }
            outB.close();
            instruction.setString(4, photo.getPath());
        } else {
            instruction.setString(4, null);
        }
        instruction.setInt(5, p.getPhone());
        instruction.setString(6, p.getPostalCode());
        instruction.setString(7,p.getEmail());
        instruction.executeUpdate();
        instruction.close();
        disconnect(conn);
    }

    @Override
    public void update(Person p) throws FileNotFoundException, SQLException, IOException {
        Connection conn;
        PreparedStatement instruction;
        conn = connect();
        instruction = conn.prepareStatement(SQL_UPDATE);
        instruction.setString(1, p.getName());
        if (p.getDateOfBirth() != null) {
            instruction.setDate(2, new java.sql.Date((p.getDateOfBirth()).getTime()));
        } else {
            instruction.setDate(2, null);
        }
        if (p.getPhoto() != null) {
            String sep = File.separator;
            File imagePerson = new File(Routes.DB.getFolderPhotos() + sep + p.getNif() + ".png");
            FileOutputStream out;
            BufferedOutputStream outB;
            out = new FileOutputStream(imagePerson);
            outB = new BufferedOutputStream(out);
            BufferedImage bi = new BufferedImage(p.getPhoto().getImage().getWidth(null),
                    p.getPhoto().getImage().getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
            bi.getGraphics().drawImage(p.getPhoto().getImage(), 0, 0, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] img = baos.toByteArray();
            for (int i = 0; i < img.length; i++) {
                outB.write(img[i]);
            }
            outB.close();
            instruction.setString(3, imagePerson.getPath());
        } else {
            instruction.setString(3, null);
            File photoFile = new File(Routes.DB.getFolderPhotos() + File.separator + p.getNif()
                    + ".png");
            photoFile.delete();
        }
        instruction.setInt(4, p.getPhone());
        instruction.setString(5, p.getPostalCode());
        instruction.setString(6, p.getEmail());
        instruction.setString(7,p.getNif());
        instruction.executeUpdate();
        instruction.close();
        disconnect(conn);
    }

    @Override
    public void deleteAll() throws Exception {
        Connection conn;
        PreparedStatement instruction;
        conn = connect();
        instruction = conn.prepareStatement(SQL_DELETE_ALL);
        System.out.println(SQL_DELETE_ALL);
        instruction.executeUpdate();
        instruction.close();
        disconnect(conn);
        File file = new File(Routes.DB.getFolderPhotos() + File.separator);
        for(File f : file.listFiles())
            f.delete();
    }

    @Override
    public int count() throws Exception {
        int count = 0;
        Connection conn;
        Statement instruction;
        ResultSet rs;
        conn = connect();
        instruction = conn.createStatement();
        rs = instruction.executeQuery(SQL_COUNT);
        if (rs.next()) {
            count = rs.getInt("total");
        }
        rs.close();
        instruction.close();
        disconnect(conn);
        return count;
    }
}

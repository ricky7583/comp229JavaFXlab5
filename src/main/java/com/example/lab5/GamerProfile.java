package com.example.lab5;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;


public class GamerProfile extends Application {
    private TextField userName, firstName, lastName, address, postalCode, province, phoneNumber, playingDate, score;
    private ObservableList<Player> players = FXCollections.observableArrayList();
    private ObservableList<String> playersListOfGames = FXCollections.observableArrayList();
    private ObservableList<String> selectedPlayerGames = FXCollections.observableArrayList();

    private Button btnSubmit = new Button("Submit");
    private Button btnAddGame = new Button("Add Game to User");
    private Button btnUpdate = new Button("Update Information");
    private ComboBox<String> listOfGames = new ComboBox<>();

    private PreparedStatement pst;
    private Connection conn;
    private Statement statement;
    private ResultSet resultSet;
    private int pagId;

    private static final String DRIVER = "oracle.jdbc.driver.OracleDriver";
    private static final String DATABASE_URL = "jdbc:oracle:thin:@199.212.26.208:1521:SQLD";
    private static final String DATABASE_USER = "COMP214F21_009_P_5";
    private static final String DATABASE_PASSWORD = "password";

    @Override
    public void start(Stage primaryStage) throws SQLException{

        //initialize declared TextFields
        userName = new TextField();
        firstName = new TextField();
        lastName = new TextField();
        address = new TextField();
        postalCode = new TextField();
        province = new TextField();
        phoneNumber = new TextField();
        playingDate = new TextField();
        score = new TextField();


        GridPane formPane = new GridPane();
        GridPane.setConstraints(formPane, 10, 10, 10 , 10);
        btnSubmit.setAlignment(Pos.BASELINE_CENTER);
        formPane.setPadding(new Insets(10,10,10,10));
        formPane.setHgap(10);
        formPane.setVgap(10);
        //Add labels to declared GridPane formPane
        formPane.add(new Label("User Name: "), 0, 0);
        formPane.add(new Label("First Name: "), 0, 1);
        formPane.add(new Label("Last Name: "),0, 2);
        formPane.add(new Label("Address: "), 0, 3);
        formPane.add(new Label("Postal Code: "), 0, 4);
        formPane.add(new Label("Province: "), 0, 5);
        formPane.add(new Label("Phone Number: "), 0, 6);

        formPane.add(new Label("What games have you played: "), 0, 10);
        formPane.add(new Label("Playing Date: "), 0, 11);
        formPane.add(new Label("Score: "), 0, 12);

        //Add input TextBoxes to formPane
        formPane.add(userName, 1, 0);
        formPane.add(firstName, 1, 1);
        formPane.add(lastName, 1, 2);
        formPane.add(address, 1, 3);
        formPane.add(postalCode, 1, 4);
        formPane.add(province, 1, 5);
        formPane.add(phoneNumber, 1, 6);
        formPane.add(listOfGames, 1, 10);
        formPane.add(playingDate, 1, 11);
        formPane.add(score, 1, 12);

        //Table of players
        TableView<Player> playerTable = new TableView<>();
        TableColumn<Player, String> colUserName = new TableColumn<>("User Name");
        colUserName.setMinWidth(100);
        colUserName.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Player, String> colFirstName = new TableColumn<>("First Name");
        colFirstName.setMinWidth(100);
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));

        TableColumn<Player, String> colLastName = new TableColumn<>("Last Name");
        colLastName.setMinWidth(100);
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));

        TableColumn<Player, String> colAddress = new TableColumn<>("Address");
        colAddress.setMinWidth(100);
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));

        TableColumn<Player, String> colPostalCode = new TableColumn<>("Postal Code");
        colPostalCode.setMinWidth(100);
        colPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        TableColumn<Player, String> colProvince = new TableColumn<>("Province");
        colProvince.setMinWidth(100);
        colProvince.setCellValueFactory(new PropertyValueFactory<>("province"));

        TableColumn<Player, String> colPhoneNumber = new TableColumn<>("Phone Number");
        colPhoneNumber.setMinWidth(150);
        colPhoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        playerTable.setItems(players);
        playerTable.getColumns().addAll(colUserName, colFirstName, colLastName, colAddress, colPostalCode, colProvince, colPhoneNumber);

        //Display players list of played games
        GridPane buttonPane = new GridPane();
        buttonPane.setPadding(new Insets(10,10,10,10));
        buttonPane.setHgap(10);
        buttonPane.add(btnSubmit,0,0);
        buttonPane.add(btnUpdate,1,0);
        buttonPane.add(btnAddGame,2,0);

        //Players game info pane
        GridPane gameInfoPane = new GridPane();
        gameInfoPane.setPadding(new Insets(10, 10, 0, 10));
        gameInfoPane.setMinWidth(150);
        gameInfoPane.setMaxWidth(250);
        //TextArea to display selected game stats
        TextArea gameStats = new TextArea();
        gameStats.setEditable(false);
        //List of selected players played games
        ListView<String> playersGames = new ListView<>();
        gameInfoPane.add(new Label("List of Games: "), 0, 0);
        gameInfoPane.add(playersGames, 0, 1);
        gameInfoPane.add(new Label("Game Stats") , 0, 2);
        gameInfoPane.add(gameStats, 0, 3);

        //Main Application layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(10, 10, 10, 10));
        mainLayout.setLeft(formPane);
        mainLayout.setCenter(playerTable);
        mainLayout.setRight(gameInfoPane);
        mainLayout.setBottom(buttonPane);

        //Add event handlers to submit button
        btnSubmit.setOnAction(e -> {
            try {
                //Database driver
                Class.forName(DRIVER);
                //Set database connection options
                conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

                //Create the prepared statement for inserting a player
                pst = conn.prepareStatement("INSERT INTO Player (username, first_name, last_name, address, postal_code, province, phone_number) VALUES (?, ?, ?, ?, ?, ?, ?)");

                //Parameters for the statement
                pst.setString(1, userName.getText());
                pst.setString(2, firstName.getText());
                pst.setString(3, lastName.getText());
                pst.setString(4, address.getText());
                pst.setString(5, postalCode.getText());
                pst.setString(6, province.getText());
                pst.setString(7, phoneNumber.getText());

                //Execute the statement
                pst.executeUpdate();

                //Alert message on player insert success
                Alert alert = new Alert(AlertType.INFORMATION, "Player has been successfully added!");
                alert.setHeaderText("Information");
                alert.setTitle("Player Added");
                alert.show();
            }
            catch (Exception ex) {
                //Alert message on error
                Alert alert = new Alert(AlertType.ERROR, "Username already exists.");
                alert.setHeaderText("Error");
                alert.setTitle("Error");
                alert.show();
            }
            finally {
                //Close connection and statement; must be surrounded with try/catch
                try {
                    pst.close();
                    conn.close();
                }
                catch (Exception ex) {}
            }

            //Repopulate the table
            populateTable();
        });

        //Add event handlers to update button
        btnUpdate.setOnAction(e -> {
            if (!userName.getText().equals("")) {
                try {
                    //Database driver
                    Class.forName(DRIVER);
                    //Set database connection options
                    conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

                    //Create the prepared statement for inserting a player
                    pst = conn.prepareStatement("UPDATE Player SET first_name=?, last_name=?, address=?, postal_code=?, province=?, phone_number=? WHERE username=?");

                    //Parameters for the statement
                    pst.setString(1, firstName.getText());
                    pst.setString(2, lastName.getText());
                    pst.setString(3, address.getText());
                    pst.setString(4, postalCode.getText());
                    pst.setString(5, province.getText());
                    pst.setString(6, phoneNumber.getText());
                    pst.setString(7, userName.getText());

                    //Execute the statement and check if update is successful
                    if (pst.executeUpdate() == 0) {
                        throw new Exception();
                    }

                    //Alert message on player insert success
                    Alert alert = new Alert(AlertType.INFORMATION, "Player information has been successfully updated!");
                    alert.setHeaderText("Information");
                    alert.setTitle("Player Information Updated");
                    alert.show();
                }
                catch (Exception ex) {
                    //Alert message on error
                    Alert alert = new Alert(AlertType.ERROR, "Username not found / cannot change username.");
                    alert.setHeaderText("Error");
                    alert.setTitle("Error");
                    alert.show();
                }
                finally {
                    //Close connection and statement; must be surrounded with try/catch
                    try { pst.close(); conn.close(); }
                    catch (Exception ex) {}
                }
            }
            else {
                //Alert message on error
                Alert alert = new Alert(AlertType.ERROR, "Username cannot be empty.");
                alert.setHeaderText("Error");
                alert.setTitle("Error");
                alert.show();
            }

            //Repopulate the table
            populateTable();

        });

        //Add event handlers to add game button
        btnAddGame.setOnAction(e -> {
            try {
                //Database driver
                Class.forName(DRIVER);
                //Set database connection options
                conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

                //Create the prepared statement for add a game
                pst = conn.prepareStatement("INSERT INTO PlayerAndGame (game_id, username, playing_date, score) VALUES (?, ?, ?, ?)");

                //Parameters for the statement
                pst.setInt(1, listOfGames.getSelectionModel().getSelectedIndex() + 1);
                pst.setString(2, userName.getText());
                pst.setString(3, playingDate.getText());
                pst.setString(4, score.getText());
                //Execute the statement
                pst.executeUpdate();

                //Alert message on player insert success
                Alert alert = new Alert(AlertType.INFORMATION, "Playing information has been successfully added!");
                alert.setHeaderText("Information");
                alert.setTitle("Information Added");
                alert.show();
            }
            catch (Exception ex) {
                //Alert message on error
                Alert alert = new Alert(AlertType.ERROR, ex.getMessage());
                alert.setHeaderText("Error");
                alert.setTitle("Error");
                alert.show();
            }
            finally {
                //Close connection and statement; must be surrounded with try/catch
                try {
                    pst.close();
                    conn.close();
                }
                catch (Exception ex) {}

                //Repopulate the table
                populateTable();

                playingDate.setText("");
                score.setText("");
            }});

        //When the user makes a selection on the table
        playerTable.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (playerTable.getSelectionModel().getSelectedItem() != null) {
                //Clears the list of the selected player's games
                selectedPlayerGames.clear();

                //Gets the selected player and assigns it to a temporary variable
                Player selected = playerTable.getSelectionModel().getSelectedItem();

                //Creates a temporary list of game titles
                List<String> gameTitles = new ArrayList<String>();

                //Goes through the List of Games to add game titles to the temporary list
                for (Game g : selected.getGamesPlayed()) { gameTitles.add(g.getTitle()); }

                //Sets the ObservableList to the data from the temporary list
                selectedPlayerGames = FXCollections.observableArrayList(gameTitles);

                //Populates the ListView with items from the ObservableList
                playersGames.setItems(selectedPlayerGames);

                //Sets TextBox values for updating
                userName.setText(selected.getUserName());
                firstName.setText(selected.getFirstName());
                lastName.setText(selected.getLastName());
                address.setText(selected.getAddress());
                postalCode.setText(selected.getPostalCode());
                province.setText(selected.getProvince());
                phoneNumber.setText(selected.getPhoneNumber());
            }
        });

        //When a game is selected from the listview
        playersGames.getSelectionModel().selectedItemProperty().addListener(e -> {
            if (playersGames.getSelectionModel().getSelectedIndex() >= 0) {
                //Gets the index of the selected game
                int index = playersGames.getSelectionModel().getSelectedIndex();

                //Gets the information of the selected player
                Player selectedPlayer = playerTable.getSelectionModel().getSelectedItem();

                //Accesses the player's game list
                List<Game> listGames = selectedPlayer.getGamesPlayed();

                //Sets the TextArea to the game information
                gameStats.setText("Score: " + listGames.get(index).getScore() + "\n" +
                        "Date: " + listGames.get(index).getDate());
            }
            else {
                gameStats.setText("");
            }
        });

        //Populate the player table
        populateTable();

        populateComboBox();

        Scene scene = new Scene(mainLayout);
        primaryStage.setMinHeight(600);
        primaryStage.setHeight(500);
        primaryStage.setMinWidth(1024);
        primaryStage.setTitle("Gamer Profile");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        GamerProfile.launch();
    }

    //Populates the player table
    protected void populateTable() {
        players.clear();
        try {
            //Database driver
            Class.forName(DRIVER);
            //Set database connection options
            conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            //Create the prepared statement for inserting a player
            pst = conn.prepareStatement("SELECT * FROM Player");

            //Set ResultSet to query
            ResultSet playerRS = pst.executeQuery();

            while (playerRS.next()) {
                //Temporary list of games for insert into new Player object
                List<Game> listOfGames = new ArrayList<Game>();

                //SQL query for players and games
                pst = conn.prepareStatement(
                        "SELECT Player.username, Games.game_id, Games.game_title, playing_date, score "
                                + "FROM Player "
                                + "JOIN PlayerAndGame ON Player.username = PlayerAndGame.username "
                                + "JOIN Games ON PlayerAndGame.game_id = Games.game_id");
                //ResultSet for player and game query
                ResultSet gameRS = pst.executeQuery();

                while (gameRS.next()) {
                    //If this game record belongs to the player
                    if (gameRS.getString(1).equals(playerRS.getString(1))) {
                        listOfGames.add(new Game(Integer.toString(gameRS.getInt(2)), gameRS.getString(3), gameRS.getString(4), gameRS.getString(5)));
                    }
                }
                //Adds a new Player object to the list of players
                players.add(new Player(playerRS.getString(1), playerRS.getString(2), playerRS.getString(3),
                        playerRS.getString(4), playerRS.getString(5), playerRS.getString(6),
                        playerRS.getString(7), listOfGames));
            }
        }
        catch (Exception ex) {}
        finally {
            //Close connection and statement; must be surrounded with try/catch
            try { pst.close(); conn.close(); }
            catch (Exception ex) {}
        }

    }

    //Populates the ComboBox
    protected void populateComboBox()
    {
        playersListOfGames.clear();
        try {
            //Database driver
            Class.forName(DRIVER);
            //Set database connection options
            conn = DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);

            statement = conn.createStatement();
            String query ="Select game_title from games";

            ResultSet gameRS = statement.executeQuery(query);

            //Adds the game titles to a list
            while (gameRS.next()) {
                playersListOfGames.add(gameRS.getString(1));
            }
        }
        catch (Exception ex) {}
        finally {
            //Close connection and statement; must be surrounded with try/catch
            try { pst.close(); conn.close(); }
            catch (Exception ex) {}
        }

        listOfGames.setItems(playersListOfGames);
    }
}

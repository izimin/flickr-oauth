package trrp.lab1.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import trrp.lab1.service.AesService;
import trrp.lab1.service.Flickr;
import trrp.lab1.model.Comment;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainFrmController {

    @FXML
    private TableView<Comment> tvComments;

    @FXML
    private TableColumn<String, Comment> clmnCommentText;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnLogout;

    @FXML
    private Button btnGetComment;

    @FXML
    public Button btnDeleteComment;

    @FXML
    public Button btnAddComment;

    @FXML
    public Button btnEditComment;

    @FXML
    public TextArea inpComment;

    @FXML
    private Pane mainPain;

    Flickr flickr;
    Long photoId = 49908593027L;

    @FXML
    void initialize() throws Exception {

        btnLogin.setOnAction(event -> {
            try {
                flickr = new Flickr();
                updateTvComments(flickr.getComments(photoId));
            } catch (Exception e) {
                e.printStackTrace();
            }
            mainPain.setDisable(false);
            btnLogout.setDisable(false);
            btnLogin.setDisable(true);
        });

        btnLogout.setOnAction(event -> {
            try {
                new AesService().clear();
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
            flickr = null;
            mainPain.setDisable(true);
            btnLogout.setDisable(true);
            btnLogin.setDisable(false);
        });

        btnGetComment.setOnAction(event -> {
            try {
                updateTvComments(flickr.getComments(photoId));
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, e.getMessage());
            }
        });

        btnAddComment.setOnAction(event -> {
            String commentText = inpComment.getText();
            if (commentText == null || commentText.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Введите текст комментария!");
                return;
            }
            try {
                Comment newComment = new Comment(flickr.addComment(photoId, commentText), commentText);
                tvComments.getItems().add(newComment);
                inpComment.setText("");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, e.getMessage());
            }
        });

        btnEditComment.setOnAction(event -> {
            String commentText = inpComment.getText();
            int selectedIndex = tvComments.getSelectionModel().getSelectedIndex();
            if (selectedIndex == -1) {
                showAlert(Alert.AlertType.ERROR, "Выберите комментарий для изменения!");
                return;
            }
            try {
                String commentId = tvComments.getSelectionModel().getSelectedItem().getId();
                flickr.editComment(photoId, commentId, commentText);
                tvComments.getItems().set(selectedIndex, new Comment(commentId, commentText));
                inpComment.setText("");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, e.getMessage());
            }
        });

        btnDeleteComment.setOnAction(event -> {
            int selectedIndex = tvComments.getSelectionModel().getSelectedIndex();
            if (selectedIndex == -1) {
                showAlert(Alert.AlertType.ERROR, "Выберите комментарий для удаления!");
                return;
            }
            try {
                flickr.deleteComment(tvComments.getSelectionModel().getSelectedItem().getId());
                tvComments.getItems().remove(selectedIndex);
            } catch (InterruptedException | ExecutionException | IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, e.getMessage());
            }
        });

        tvComments.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                inpComment.setText(newSelection.get_content());
            }
        });

        clmnCommentText.setCellValueFactory(new PropertyValueFactory<>("_content"));
    }

    void updateTvComments(List<Comment> comments) {
        tvComments.getItems().clear();
        tvComments.getItems().addAll(comments);
        inpComment.setText("");
    }

    void showAlert(Alert.AlertType alertType, String text) {
        Alert errorAlert = new Alert(alertType);
        errorAlert.setContentText(text);
        errorAlert.showAndWait();
    }
}

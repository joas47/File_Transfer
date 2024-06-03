module com.example.filetransfer {
    requires javafx.controls;
    requires org.json;
    requires java.sql;


    opens com.example.filetransfer;
    exports com.example.filetransfer;
}
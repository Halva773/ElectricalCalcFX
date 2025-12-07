module com.electrical {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires jbcrypt;
    requires org.slf4j;

    opens com.electrical to javafx.fxml;
    opens com.electrical.controller to javafx.fxml;
    opens com.electrical.model to javafx.base;

    exports com.electrical;
    exports com.electrical.controller;
    exports com.electrical.model;
    exports com.electrical.service;
    exports com.electrical.dao;
    exports com.electrical.util;
}



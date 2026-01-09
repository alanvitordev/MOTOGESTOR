/*
 * The MIT License
 *
 * Copyright 2025 Equipe MotoGestor.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package br.com.motogestor.DAL;

import java.io.FileInputStream;
import java.sql.*; 
import java.util.Properties;

/**
 * CONEXÃO COM O BANCO DE DADOS
 * @author EquipeMotoGestor
 * @version 1.2
 */
public class ModuloConexao {

    public static Connection conector() {

        Properties props = new Properties();
        Connection conexao = null;
        String driver = "com.mysql.cj.jdbc.Driver";

       try (FileInputStream fis = new FileInputStream("db.properties")) { // O arquivo fecha sozinho aqui

    props.load(fis);
    Class.forName(driver);

    conexao = DriverManager.getConnection(

        props.getProperty("db.url"), 
        props.getProperty("db.user"), 
        props.getProperty("db.password")
    );

    System.out.println("Conexão estabelecida com sucesso!");

    return conexao;

} catch (Exception e) {

    System.out.println("Erro na conexão ou no arquivo de propriedades: " + e);

    return null;
}

    }

}
/*
 * The MIT License
 *
 * Copyright 2025 karla.
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
package br.com.motogestor.telas;

import java.sql.*;
import br.com.motogestor.DAL.ModuloConexao;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
//a linha abaixo importa recursos da biblioteca rs2xml.jar...
import net.proteanit.sql.DbUtils; //biblioteca que será utilizada como recurso para preencher a tabela com os dados do fornecedor.
public class TelaFornecedor extends javax.swing.JInternalFrame {

    //PARAMETROS DA QUERY
    
    Connection conexao = null; //chamando a variável conexao criada em ModuloConexao, 'Connection é um framework do pacote importado.
    PreparedStatement pst = null; //PreparedStatement também é um framwework de manipução dos dados em sql, responsável pela consulta.
    ResultSet rs = null; //ResultSet servirá para exibir o resultado das instruções executadas no Java.
    
     private int idUser;

    public TelaFornecedor(int idUser) {
        initComponents();

        //CONEXAO SQL
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarFornecedor();
    }

    private void adicionarFornecedor() {

        String sql_valida = "SELECT 1 FROM tbfornecedor WHERE cnpj = ?";

        String sql_adicionar = "insert into tbfornecedor (fornecedor, cnpj, telefone, email, status, idusuario) values (?, ?, ?, ?, 'Ativo', ?)";

        try {

            String nomeFornecedor = txtNomeFornecedor.getText().toString().trim();
            String cnpjFornecedor = txtCnpjFornecedor.getText().toString().trim();
            String foneFornecedor = txtFoneFornecedor.getText().toString().trim();
            String emailFornecedor = txtEmailFornecedor.getText().toString().trim();

            if (nomeFornecedor.isEmpty() || cnpjFornecedor.isEmpty() || foneFornecedor.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, cnpjFornecedor); // Checa o CNPJ
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                JOptionPane.showMessageDialog(null, "Atenção: Este CNPJ já está cadastrado!");
                return; // Para o método
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, nomeFornecedor);
            pst.setString(2, cnpjFornecedor);
            pst.setString(3, txtFoneFornecedor.getText().replaceAll("[^0-9]", ""));
            pst.setString(4, emailFornecedor);
            pst.setInt(5, TelaPrincipal.idUser);

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Fornecedor adicionado com sucesso!");

                limparCampos();

                pesquisarFornecedor();

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }
    }

    private void pesquisarFornecedor() {

        String sql = "SELECT idfornecedor as ID, fornecedor as FORNECEDOR, cnpj as CNPJ, telefone as FONE,"
                + " email as EMAIL, "
                + "CASE WHEN status = TRUE THEN 'Ativo' ELSE 'Inativo' END AS STATUS " + "FROM tbfornecedor WHERE fornecedor LIKE ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtFornecedor.getText() + "%");
            rs = pst.executeQuery();

            tblFornecedor.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void preencherCamposFornecedor() {

        int preencher = tblFornecedor.getSelectedRow();

        txtIdFornecedor.setText(tblFornecedor.getModel().getValueAt(preencher, 0).toString());
        txtNomeFornecedor.setText(tblFornecedor.getModel().getValueAt(preencher, 1).toString());
        txtCnpjFornecedor.setText(tblFornecedor.getModel().getValueAt(preencher, 2).toString());
        txtFoneFornecedor.setText(tblFornecedor.getModel().getValueAt(preencher, 3).toString());
        txtEmailFornecedor.setText(tblFornecedor.getModel().getValueAt(preencher, 4).toString());

        logicaTbl();

    }

    private void atualizarFornecedor() {

        String sql_valida = "SELECT 1 FROM tbfornecedor WHERE cnpj = ? AND idfornecedor <> ?";

        String sql = "update tbfornecedor set fornecedor =?, cnpj =?, telefone =?, email =?, idusuario=?, data = now() where idfornecedor =?";

        try {

            String nomeFornecedor = txtNomeFornecedor.getText().toString().trim();
            String cnpjFornecedor = txtCnpjFornecedor.getText().toString().trim();
            String foneFornecedor = txtFoneFornecedor.getText().toString().trim();
            String emailFornecedor = txtEmailFornecedor.getText().toString().trim();
            String idFornecedorStr = txtIdFornecedor.getText().toString().trim();

            if (nomeFornecedor.isEmpty() || cnpjFornecedor.isEmpty()
                    || foneFornecedor.isEmpty() || idFornecedorStr.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return;
            }

            int idFornecedor;

            try {

                idFornecedor = Integer.parseInt(idFornecedorStr);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do fornecedor é inválido.");
                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, cnpjFornecedor);
            pst_valida.setInt(2, idFornecedor);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {

                JOptionPane.showMessageDialog(null, "Atenção: Este CNPJ já está cadastrado em outro fornecedor!");
                return;
            }

            pst = conexao.prepareStatement(sql);

            pst.setString(1, nomeFornecedor);
            pst.setString(2, cnpjFornecedor);
            pst.setString(3, txtFoneFornecedor.getText().replaceAll("[^0-9]", ""));
            pst.setString(4, emailFornecedor);
            pst.setInt(5, TelaPrincipal.idUser);
            pst.setInt(6, idFornecedor);

            int fornecedorAdd = pst.executeUpdate();

            if (fornecedorAdd > 0) {

                JOptionPane.showMessageDialog(null, "Dados alterados com sucesso!");

                limparCampos();

                logicaUpdate();

                pesquisarFornecedor();

            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void inativarFornecedor() {

        int inativacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja INATIVAR este fornecedor?",
                "Atenção", JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbfornecedor SET status = 'Inativo', idusuario=? WHERE idfornecedor = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdFornecedor.getText());

                int fornecedorInativado = pst.executeUpdate();

                if (fornecedorInativado > 0) {

                    JOptionPane.showMessageDialog(null, "Fornecedor inativado com sucesso!");

                    limparCampos();

                    logicaInativar();

                    pesquisarFornecedor();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarFornecedor() {

        int ativacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja ATIVAR este fornecedor?",
                "Atenção", JOptionPane.YES_NO_OPTION);

        if (ativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbfornecedor SET status = TRUE, idusuario=? WHERE idfornecedor = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdFornecedor.getText());

                int fornecedorAtivado = pst.executeUpdate();

                if (fornecedorAtivado >= 1) {

                    JOptionPane.showMessageDialog(null, "Fornecedor ativado com sucesso!");

                    limparCampos();

                    logicaAtivar();

                    pesquisarFornecedor();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblFornecedor.setEnabled(true);
        txtFornecedor.setEnabled(true);

    }

    private void logicaTbl() {

        btnAdicionar.setEnabled(false);

        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);

    }

    private void logicaAtivar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblFornecedor.setEnabled(true);
        txtFornecedor.setEnabled(true);

    }

    private void logicaInativar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblFornecedor.setEnabled(true);
        txtFornecedor.setEnabled(true);

    }

    private void limpezaGeralTbl() {

        txtNomeFornecedor.setText(null);
        txtCnpjFornecedor.setText(null);
        txtFoneFornecedor.setText(null);
        txtEmailFornecedor.setText(null);

        btnAdicionar.setEnabled(true);

        tblFornecedor.setEnabled(true);
        txtFornecedor.setEnabled(true);

        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void limparCampos() {

        txtNomeFornecedor.setText(null);
        txtCnpjFornecedor.setText(null);
        txtFoneFornecedor.setText(null);
        txtEmailFornecedor.setText(null);

    }
    
   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        txtEmailFornecedor = new javax.swing.JTextField();
        btnAdicionar = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        btnAlterar = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        txtIdFornecedor = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        btnLimparDados = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        pnlFornecedor = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        txtFornecedor = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblFornecedor = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnAtivar = new javax.swing.JButton();
        btnInativar = new javax.swing.JButton();
        txtCnpjFornecedor = new javax.swing.JTextField();
        txtNomeFornecedor = new javax.swing.JTextField();
        txtFoneFornecedor = new javax.swing.JFormattedTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR FORNECEDOR");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel5.setText(" E-MAIL:");

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarPessoas.png"))); // NOI18N
        btnAdicionar.setText("Adicionar ");
        btnAdicionar.setToolTipText("Adicionar Fornecedor");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        jLabel4.setText("* TELEFONE:");

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarPessoas.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Dados");
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        jLabel7.setText("ID:");

        txtIdFornecedor.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdFornecedor.setEnabled(false);
        txtIdFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdFornecedorActionPerformed(evt);
            }
        });

        jLabel9.setText(" * CNPJ:");

        btnLimparDados.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        btnLimparDados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/clean.png"))); // NOI18N
        btnLimparDados.setText("Limpar Campos");
        btnLimparDados.setToolTipText("Limpar Dados");
        btnLimparDados.setPreferredSize(new java.awt.Dimension(20, 20));
        btnLimparDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparDadosActionPerformed(evt);
            }
        });

        jLabel2.setText("* NOME:");

        pnlFornecedor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlFornecedorMouseClicked(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("* CAMPOS OBRIGATÓRIOS");

        txtFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFornecedorActionPerformed(evt);
            }
        });
        txtFornecedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtFornecedorKeyReleased(evt);
            }
        });

        tblFornecedor = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;

            }
        };
        tblFornecedor.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "FORNECEDOR", "CNPJ", "FONE", "EMAIL", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblFornecedor.setFocusable(false);
        tblFornecedor.getTableHeader().setReorderingAllowed(false);
        tblFornecedor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblFornecedorMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblFornecedor);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel1.setText("FORNECEDOR:");

        javax.swing.GroupLayout pnlFornecedorLayout = new javax.swing.GroupLayout(pnlFornecedor);
        pnlFornecedor.setLayout(pnlFornecedorLayout);
        pnlFornecedorLayout.setHorizontalGroup(
            pnlFornecedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFornecedorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFornecedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlFornecedorLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
                        .addComponent(jLabel6)
                        .addGap(41, 41, 41))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFornecedorLayout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())))
        );
        pnlFornecedorLayout.setVerticalGroup(
            pnlFornecedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFornecedorLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(pnlFornecedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
        );

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Fornecedor");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        txtCnpjFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCnpjFornecedorActionPerformed(evt);
            }
        });

        txtNomeFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNomeFornecedorActionPerformed(evt);
            }
        });

        try {
            txtFoneFornecedor.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##) #####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(21, 21, 21)
                                        .addComponent(jLabel7)
                                        .addGap(10, 10, 10))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addComponent(txtIdFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(702, 702, 702))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addGap(27, 27, 27)
                                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(54, 54, 54)
                                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(65, 65, 65)
                                        .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(65, 65, 65)
                                        .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtFoneFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(55, 55, 55)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtEmailFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE)
                                    .addComponent(txtCnpjFornecedor)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(pnlFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNomeFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txtCnpjFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(txtEmailFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFoneFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(51, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        //chamando o método criado dentro da classe AdicionarFornecedor();

        adicionarFornecedor();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        atualizarFornecedor();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void txtIdFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdFornecedorActionPerformed
        //ADICIONEI UM EVENTO SEM QUERER AQUI A CAIXA DE TEXTO EM FRENTE AO "NOME:"
    }//GEN-LAST:event_txtIdFornecedorActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void txtFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFornecedorActionPerformed
        //ADICIONEI O EVENTO ERRO PARA O CAMPO DE TEXTO "PESQUISA".
    }//GEN-LAST:event_txtFornecedorActionPerformed

    private void txtFornecedorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtFornecedorKeyReleased
        //chamando o método PesquisarCliente();

        pesquisarFornecedor();
    }//GEN-LAST:event_txtFornecedorKeyReleased

    private void tblFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblFornecedorMouseClicked
        // o código evento que será usado para setar os campos da tabela ao clicar com o mouse em algum campo.

        //chamando o método para puxar oque foi criado dentro do mesmo.

        preencherCamposFornecedor();
    }//GEN-LAST:event_tblFornecedorMouseClicked

    private void pnlFornecedorMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlFornecedorMouseClicked

        tblFornecedor.clearSelection();
        limpezaGeralTbl();
    }//GEN-LAST:event_pnlFornecedorMouseClicked

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarFornecedor();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed

        inativarFornecedor();
    }//GEN-LAST:event_btnInativarActionPerformed

    private void txtCnpjFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCnpjFornecedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCnpjFornecedorActionPerformed

    private void txtNomeFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNomeFornecedorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNomeFornecedorActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlFornecedor;
    private javax.swing.JTable tblFornecedor;
    private javax.swing.JTextField txtCnpjFornecedor;
    private javax.swing.JTextField txtEmailFornecedor;
    private javax.swing.JFormattedTextField txtFoneFornecedor;
    private javax.swing.JTextField txtFornecedor;
    private javax.swing.JTextField txtIdFornecedor;
    private javax.swing.JTextField txtNomeFornecedor;
    // End of variables declaration//GEN-END:variables
}

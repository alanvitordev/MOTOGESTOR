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

import br.com.motogestor.DAL.ModuloConexao;
import br.com.motogestor.telas.SubTelaPesqFornecedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;


public class TelaMarcas extends javax.swing.JInternalFrame {
    
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private int idUser;

    public TelaMarcas(int idUser) {
        initComponents();

        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarMarca();
        
    }

    private void adicionarMarca() {

        String sql_valida = "SELECT 1 FROM tbmarcas WHERE descricao = ?";
        
        String sql_fornecedor = "select idfornecedor from tbfornecedor where fornecedor = ? ";
     
        String fornecedorNome = txtFornecedor.getText().trim();

        String sql_adicionar = "insert into tbmarcas (descricao, idusuario, status, idfornecedor) values (?, ?, 'Ativa', ?)";
        
        

        try {

                String descMarca = txtMarca.getText().toString().trim();

            if (descMarca.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, descMarca); // Checa a descrição
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Uma marca com esta descrição já está cadastrada!");
                return;
            }
            
            Integer fornecedorId = null; 
            
            PreparedStatement pst_fornecedor = conexao.prepareStatement(sql_fornecedor);
            pst_fornecedor.setString(1, fornecedorNome); // Checa a descrição
            ResultSet rs_fornecedor = pst_fornecedor.executeQuery();

            if (rs_fornecedor.next()) {
                
               fornecedorId = rs_fornecedor.getInt("idfornecedor");
               
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, descMarca);
            pst.setInt(2, this.idUser);
            pst.setInt(3, fornecedorId);

            int incluida = pst.executeUpdate();

            if (incluida > 0) {

                JOptionPane.showMessageDialog(null, "Marca adicionada com sucesso!");
                
                limpezaDados();
  
                pesquisarMarca();

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void pesquisarMarca() {

        String sql = "SELECT m.idmarca as ID, m.descricao as MARCA, f.fornecedor  as FORNECEDOR, m.status as STATUS "
                + "FROM tbmarcas m "
                + "left join tbfornecedor f on (m.idfornecedor = f.idfornecedor) "
                + "where descricao LIKE ? ";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtPesqMarca.getText() + "%");
            rs = pst.executeQuery();

            tblMarcas.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void preencherCampoMarca() {

    int preencher = tblMarcas.getSelectedRow();

    // Captura os objetos antes de converter para evitar o erro
    Object objId = tblMarcas.getModel().getValueAt(preencher, 0);
    Object objMarca = tblMarcas.getModel().getValueAt(preencher, 1);
    Object objFornecedor = tblMarcas.getModel().getValueAt(preencher, 2);

    // Se for null (vazio), define como texto em branco (""), senão converte para String
    txtIdMarca.setText(objId != null ? objId.toString() : "");
    txtMarca.setText(objMarca != null ? objMarca.toString() : "");
    txtFornecedor.setText(objFornecedor != null ? objFornecedor.toString() : "");

    logicaTbl();

}

    private void atualizarMarca() {

        String sql_valida = "SELECT 1 FROM tbmarcas WHERE descricao = ? AND idmarca <> ?";
        
         String sql_fornecedor = "select idfornecedor from tbfornecedor where fornecedor = ? ";
     
        String fornecedorNome = txtFornecedor.getText().trim();

        String sql = "update tbmarcas set descricao =?, idusuario=?, idfornecedor=?, data = now() where idmarca =?";

        try {

            String descMarca = txtMarca.getText().toString().trim();
            String idMarcaStr = txtIdMarca.getText().trim();

            if (descMarca.isEmpty() || idMarcaStr.isEmpty()) {
                
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return; 
            }

            int idMarca;

            try {
                
                idMarca = Integer.parseInt(idMarcaStr);
                
            } catch (NumberFormatException e) {
                
                JOptionPane.showMessageDialog(null, "Erro: ID da marca é inválido.");
                
                return;
            }

            Integer fornecedorId = null; 
            
            PreparedStatement pst_fornecedor = conexao.prepareStatement(sql_fornecedor);
            pst_fornecedor.setString(1, fornecedorNome); 
            ResultSet rs_fornecedor = pst_fornecedor.executeQuery();
           
            
            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, descMarca); // Checa a descrição
            pst_valida.setInt(2, idMarca); 

            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Esta descrição já está cadastrada em outra marca!");
                return; // Para o método
            }
            
            if (rs_fornecedor.next()) {
                
               fornecedorId = rs_fornecedor.getInt("idfornecedor");
               
            } else {
              
                JOptionPane.showMessageDialog(null, "Fornecedor não encontrado, tente novamente!");
                
                return;
            }

            pst = conexao.prepareStatement(sql);

            pst.setString(1, descMarca);
            pst.setInt(2, this.idUser);
            pst.setInt(3, fornecedorId);
            pst.setInt(4, idMarca); 
            

          
            int marcaAdd = pst.executeUpdate(); 

            if (marcaAdd > 0) {

                JOptionPane.showMessageDialog(null, "Marca alterada com sucesso!");

                limpezaDados();
                logicaUpdate();
                pesquisarMarca();

            } else {
          
                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, e);
        }

    }
    
    private void inativarMarca() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja INATIVAR esta marca?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmarcas SET status = 'Inativa', idusuario =? WHERE idmarca = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMarca.getText()); // pega o número do ID

                int inativada = pst.executeUpdate();

                if (inativada > 0) {
                    JOptionPane.showMessageDialog(null, "Marca inativado com sucesso!");

                    limpezaDados();
                    logicaInativar();
                    pesquisarMarca();
               
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarMarca() {

        int ativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja ATIVAR esta marca?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (ativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmarcas SET status = 'Ativa', idusuario=? WHERE idmarca = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMarca.getText()); // pega o número do ID

                int ativada = pst.executeUpdate();

                if (ativada >= 1) {
                    JOptionPane.showMessageDialog(null, "Marca ativada com sucesso!");

                    limpezaDados();
                    logicaAtivar();
                    pesquisarMarca();
                    
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }
    
    private void pesquisarFornecedor() {

        try {
            
            SubTelaPesqFornecedor fornecedor = new SubTelaPesqFornecedor(null, true); // true = modal
            fornecedor.setVisible(true);

            // se algum serviço foi selecionado, preenche os campos da OS
            if (fornecedor.fornecedorSelecionado != null) {

                txtFornecedor.setText(fornecedor.fornecedorSelecionado);

            }
            
        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, "Erro ao pesquisar o fornecedor! " + e);
            
        }

    }
    
    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblMarcas.setEnabled(true);
        txtPesqMarca.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
    }
             
     private void logicaTbl () {
        
        btnAdicionar.setEnabled(false);
        
        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);
        tblMarcas.setEnabled(true);
        txtPesqMarca.setEnabled(true);
    }
    
    private void logicaAtivar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblMarcas.setEnabled(true);
        txtPesqMarca.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
    }
    
    
    private void logicaInativar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblMarcas.setEnabled(true);
        txtPesqMarca.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
       
    }
    
       private void limpezaGeralTbl() { 
        
        txtMarca.setText(null);
        txtIdMarca.setText(null);
        txtPesqMarca.setText(null);
        txtFornecedor.setText(null);
        
        btnAdicionar.setEnabled(true);
        tblMarcas.setEnabled(true);
        txtPesqMarca.setEnabled(true);
        
        
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        btnAdicionar.setEnabled(true);
    }
    
    
    private void limpezaDados() { 

        txtMarca.setText(null);
        txtIdMarca.setText(null);
        txtPesqMarca.setText(null);
        txtFornecedor.setText(null);
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        btnAtivar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        txtMarca = new javax.swing.JTextField();
        btnPesqForn = new javax.swing.JLabel();
        txtFornecedor = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        pnlMarcas = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMarcas = new javax.swing.JTable();
        txtIdMarca = new javax.swing.JTextField();
        txtPesqMarca = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btnLimparDados = new javax.swing.JButton();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        btnInativar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR MARCA");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Véiculo");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 44));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        jLabel1.setText("* MARCA:");

        btnPesqForn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqForn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqFornMouseClicked(evt);
            }
        });

        txtFornecedor.setEditable(false);

        jLabel5.setText("FORNECEDOR:");

        pnlMarcas.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlMarcas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlMarcasMouseClicked(evt);
            }
        });

        tblMarcas = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;

            }
        };
        tblMarcas.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "ID", "MARCA", "FORNECEDOR", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMarcas.setFocusable(false);
        tblMarcas.getTableHeader().setReorderingAllowed(false);
        tblMarcas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMarcasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMarcas);

        txtIdMarca.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdMarca.setEnabled(false);
        txtIdMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMarcaActionPerformed(evt);
            }
        });

        txtPesqMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesqMarcaActionPerformed(evt);
            }
        });
        txtPesqMarca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqMarcaKeyReleased(evt);
            }
        });

        jLabel7.setText("ID:");

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel9.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel9.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel2.setText("MARCA:");

        javax.swing.GroupLayout pnlMarcasLayout = new javax.swing.GroupLayout(pnlMarcas);
        pnlMarcas.setLayout(pnlMarcasLayout);
        pnlMarcasLayout.setHorizontalGroup(
            pnlMarcasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMarcasLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        pnlMarcasLayout.setVerticalGroup(
            pnlMarcasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMarcasLayout.createSequentialGroup()
                .addContainerGap(27, Short.MAX_VALUE)
                .addGroup(pnlMarcasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPesqMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

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

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarDocumento.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Mecânico");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarDocumento.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Dados");
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Véiculo");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 44));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlMarcas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                                .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnPesqForn)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 73, Short.MAX_VALUE)
                                .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(61, 61, 61)
                                .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(34, 34, 34)))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlMarcas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)
                        .addComponent(txtMarca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5))
                    .addComponent(btnPesqForn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23))
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarMarca();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void btnPesqFornMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqFornMouseClicked
        pesquisarFornecedor();
    }//GEN-LAST:event_btnPesqFornMouseClicked

    private void tblMarcasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMarcasMouseClicked
        // o código evento que será usado para setar os campos da tabela ao clicar com o mouse em algum campo.

        //chamando o método para puxar oque foi criado dentro do mesmo.

        preencherCampoMarca();
        
    }//GEN-LAST:event_tblMarcasMouseClicked

    private void txtIdMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMarcaActionPerformed

    }//GEN-LAST:event_txtIdMarcaActionPerformed

    private void txtPesqMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqMarcaActionPerformed

    }//GEN-LAST:event_txtPesqMarcaActionPerformed

    private void txtPesqMarcaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqMarcaKeyReleased

        pesquisarMarca();
    }//GEN-LAST:event_txtPesqMarcaKeyReleased

    private void pnlMarcasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMarcasMouseClicked

        tblMarcas.clearSelection();

        limpezaGeralTbl();
    }//GEN-LAST:event_pnlMarcasMouseClicked

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limpezaDados();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarMarca();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        atualizarMarca();        // TODO add your handling code here:
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        inativarMarca();
    }//GEN-LAST:event_btnInativarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel btnPesqForn;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlMarcas;
    private javax.swing.JTable tblMarcas;
    private javax.swing.JTextField txtFornecedor;
    private javax.swing.JTextField txtIdMarca;
    private javax.swing.JTextField txtMarca;
    private javax.swing.JTextField txtPesqMarca;
    // End of variables declaration//GEN-END:variables
}

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
import net.proteanit.sql.DbUtils;

public class TelaProduto extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private int idUser;

    public TelaProduto(int idUser) {
        
        initComponents();

        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarProduto();
    }

    private void adicionarProduto() {
        
        String sql_valida = "SELECT 1 FROM tbprodutos WHERE produto = ?";

        String sql_adicionar = "insert into tbprodutos (produto, descricao, situacao, status, idmarca, idusuario) values (?, ?, ?, 'Ativo', ?, ?)";

        try {

            String nomeProduto = txtProd.getText().toString().trim();
            String nomeMarca = txtMarcaProd.getText().trim();
            String situacao = cboStatusProd.getSelectedItem().toString().trim();
            String descricao = txtDescProd.getText().toString().trim();

            if (situacao.equals(" ") || nomeProduto.isEmpty() || nomeMarca.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, nomeProduto);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                JOptionPane.showMessageDialog(null, "Atenção: Um produto com este nome já está cadastrado!");
                txtIdProduto.setText(null);
                txtDataProd.setText(null);

                cboStatusProd.setSelectedItem(" ");
                txtProd.setText(null);
                txtDescProd.setText(null);

                return;
            }

            Integer idMarca = null;
            String sqlBuscaMarca = "SELECT idmarca FROM tbmarcas WHERE descricao = ?";

            try (PreparedStatement pstBusca = conexao.prepareStatement(sqlBuscaMarca)) {
                
                pstBusca.setString(1, nomeMarca);
                
                try (ResultSet rsBusca = pstBusca.executeQuery()) {
                    
                    if (rsBusca.next()) {
                        
                        idMarca = rsBusca.getInt("idmarca");
                        
                    } else {
                        
                        JOptionPane.showMessageDialog(null, "Marca '" + nomeMarca + "' não encontrada! Cadastre-a primeiro.");
                        return; // Para a execução
                    }
                }
            }

            pst = conexao.prepareStatement(sql_adicionar, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, nomeProduto); // <-- Usei a variável que criei acima
            pst.setString(2, txtDescProd.getText().toString().trim());

            pst.setString(3, cboStatusProd.getSelectedItem().toString().trim());
            pst.setInt(4, idMarca);
            pst.setInt(5, TelaPrincipal.idUser);

            if (cboStatusProd.getSelectedItem().toString().trim().equals(" ") || nomeProduto.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;

            }

            int adicionado = pst.executeUpdate(); 

            if (adicionado > 0) {

                ResultSet rs = pst.getGeneratedKeys();
                int idProdutoGerado = 0;

                if (rs.next()) {
                    idProdutoGerado = rs.getInt(1);
                }

                criarEstoqueInicial(idProdutoGerado);
                criarPrecoInicial(idProdutoGerado);

                JOptionPane.showMessageDialog(null, "Produto adicionado com sucesso!");
                
                limparCampos();
                logicaInsert();
                pesquisarProduto();

            }

        } catch (Exception e) {
            
            System.out.println("Erro ao cadastrar produto/estoque " + e);

            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void criarEstoqueInicial(int idProduto) {

        String sql = "INSERT INTO tbestoque (codproduto, quantidade) VALUES (?, ?)";

        try {
            PreparedStatement pstEstoque = conexao.prepareStatement(sql);
            pstEstoque.setInt(1, idProduto);
            pstEstoque.setDouble(2, 0.00); // Estoque inicial zero

            pstEstoque.executeUpdate();

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, "Erro ao criar estoque inicial: " + e.getMessage());
        }
    }

    private void criarPrecoInicial(int idProduto) {

        String sql = "INSERT INTO tbprecificacao (codproduto, valorcusto, margemlucro, valorvenda) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement pstPreco = conexao.prepareStatement(sql);
            pstPreco.setInt(1, idProduto);
            pstPreco.setDouble(2, 0.00); // custo inicial zero
            pstPreco.setDouble(3, 0.00); // margem inicial zero
            pstPreco.setDouble(4, 0.00); // venda inicial zero

            pstPreco.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar preço inicial: " + e.getMessage());
        }
    }

    private void pesquisarProduto() {

        String sql = "SELECT "
                + "pro.idproduto as ID, "
                + "pro.produto as PRODUTO, "
                + "mar.descricao as MARCA, "
                + "pro.descricao as DESCRIÇÃO, "
                + "(select valorvenda from tbprecificacao where codproduto = pro.idproduto) as VALOR, "
                + "pro.situacao as SITUAÇÃO, "
                + "date_format(pro.data,'%d/%m/%Y - %H:%i') as DATA, "
                + "CASE WHEN pro.status = TRUE THEN 'Ativo' ELSE 'Inativo' END AS STATUS "
                + "FROM tbprodutos pro " // <-- Alias 'pro'
                + "LEFT JOIN tbmarcas mar ON pro.idmarca = mar.idmarca " // <-- NOVO JOIN
                + "WHERE pro.produto LIKE ?"; // <-- Alias 'pro'

        try {

            pst = conexao.prepareStatement(sql);

           
            pst.setString(1, txtPesqProduto.getText() + "%"); 
            rs = pst.executeQuery();

            tblProduto.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }
    }

    private void setarCampos() {
        try {
            
            int setar = tblProduto.getSelectedRow();
            if (setar == -1) {
                
                return; 
            }
            
            Object valorId = tblProduto.getModel().getValueAt(setar, 0);       // ID
            Object valorProduto = tblProduto.getModel().getValueAt(setar, 1);  // PRODUTO
            Object valorMarca = tblProduto.getModel().getValueAt(setar, 2);    // MARCA
            Object valorDesc = tblProduto.getModel().getValueAt(setar, 3);     // DESCRIÇÃO
         
            Object valorSituacao = tblProduto.getModel().getValueAt(setar, 5); // SITUAÇÃO
            Object valorData = tblProduto.getModel().getValueAt(setar, 6);     // DATA

            // Tratamento de valores nulos
            String id = (valorId == null) ? "" : valorId.toString();
            String produto = (valorProduto == null) ? "" : valorProduto.toString();
            String marca = (valorMarca == null) ? "" : valorMarca.toString();
            String descricao = (valorDesc == null) ? "" : valorDesc.toString();
            String situacao = (valorSituacao == null) ? "" : valorSituacao.toString();
            String data = (valorData == null) ? "" : valorData.toString();

            // Preenche os campos
            txtIdProduto.setText(id);
            txtProd.setText(produto);
            txtMarcaProd.setText(marca);            
            txtDescProd.setText(descricao);
            txtDataProd.setText(data);
            cboStatusProd.setSelectedItem(situacao);
            
            logicaTbl();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao selecionar linha: " + e.getMessage());
        }
    }

    private void alterarProduto() {

        String sql_valida = "SELECT 1 FROM tbprodutos WHERE produto = ? AND idproduto <> ?";

        String sql_alterar = "update tbprodutos set produto = ?, descricao = ?,"
                + " situacao = ?, idmarca = ?, idusuario = ?, data = now() where idproduto = ?";

        try {

            String nomeProduto = txtProd.getText().toString().trim();

            String idProdutoStr = txtIdProduto.getText().trim();

            String nomeMarca = txtMarcaProd.getText().trim();

            // Valida antes de tentar converter para int
            if (nomeProduto.isEmpty() || idProdutoStr.isEmpty() || nomeMarca.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            int idProduto;

            try {
                
                idProduto = Integer.parseInt(idProdutoStr);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do produto é inválido.");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, nomeProduto);
            pst_valida.setInt(2, idProduto);

            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Um produto com este nome já está cadastrado!");

                return;
            }

            Integer idMarca = null;
            
            String sqlBuscaMarca = "SELECT idmarca FROM tbmarcas WHERE descricao = ?";

            try (PreparedStatement pstBusca = conexao.prepareStatement(sqlBuscaMarca)) {
                
                pstBusca.setString(1, nomeMarca);
                
                try (ResultSet rsBusca = pstBusca.executeQuery()) {
                    
                    if (rsBusca.next()) {
                        
                        idMarca = rsBusca.getInt("idmarca");
                        
                    } else {
                        
                        JOptionPane.showMessageDialog(null, "Marca '" + nomeMarca + "' não encontrada! Cadastre-a primeiro.");
                        return; // Para a execução
                    }
                }
            }

            
            if (txtProd.getText().trim().isEmpty()) {
                
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return; // Sai do método se a validação falhar
            }

            pst = conexao.prepareStatement(sql_alterar);

            pst.setString(1, nomeProduto);
            pst.setString(2, txtDescProd.getText().toString().trim());

            pst.setString(3, cboStatusProd.getSelectedItem().toString());
            pst.setInt(4, idMarca);
            pst.setInt(5, TelaPrincipal.idUser);
            pst.setInt(6, idProduto);
            
            
            int alterado = pst.executeUpdate();

            if (alterado > 0) {

                JOptionPane.showMessageDialog(null, "Produto alterado com sucesso!");

                limparCampos();
                logicaUpdate();
                pesquisarProduto();
                
            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void inativarProduto() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja INATIVAR este produto?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_OPTION) {

            String sql = "UPDATE tbprodutos SET status = 'Inativo', idusuario=? WHERE idproduto = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdProduto.getText()); // pega o número do ID

                int osInativada = pst.executeUpdate();

                if (osInativada > 0) {
                    JOptionPane.showMessageDialog(null, "Produto inativado com sucesso!");

                    limparCampos();
                    logicaInativar();
                    pesquisarProduto();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarProduto() {
    
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja ATIVAR este produto?", "Atenção!", JOptionPane.YES_NO_OPTION);

       
        if (confirma == JOptionPane.YES_NO_OPTION) {
            
            String sql = "UPDATE tbprodutos SET status = 'Ativo', idusuario=? WHERE idproduto = ?";

            try {
                
                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdProduto.getText());

                int ativado = pst.executeUpdate();

                if (ativado >= 1) {
                    
                    JOptionPane.showMessageDialog(null, "Produto ativado com sucesso!");
                }

                limparCampos();
                logicaAtivar();
                pesquisarProduto();
                

            } catch (Exception e) {
                
                JOptionPane.showMessageDialog(null, "Erro ao ativar produto: " + e.getMessage());
            }
        }
    }

    private void pesquisarMarca() {

        try {
            SubTelaPesqMarca marca = new SubTelaPesqMarca(null, true); // true = modal
            marca.setVisible(true);

            if (marca.marcaSelecionada != null) {

                txtMarcaProd.setText(marca.marcaSelecionada);

            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao pesquisar a marca! " + e);
        } finally {

        }

    }
    
    private void logicaInsert() {

        btnAdicionar.setEnabled(false);

        btnAlterar.setEnabled(true);
        tblProduto.setEnabled(true);
        txtPesqProduto.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);

    }

    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblProduto.setEnabled(true);
        txtPesqProduto.setEnabled(true);
        
    }
    
    
     private void logicaTbl () {
        
        btnAdicionar.setEnabled(false);
        
        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);
        
    }
    
    private void logicaAtivar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblProduto.setEnabled(true);
        txtPesqProduto.setEnabled(true);
        
    }
    
    
    private void logicaInativar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblProduto.setEnabled(true);
        txtPesqProduto.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
    }
    
    private void limpezaGeralTbl() { 
        
       txtMarcaProd.setText(null); 
       txtIdProduto.setText(null);
       txtDataProd.setText(null);
       cboStatusProd.setSelectedItem(" ");
       txtProd.setText(null);
       txtDescProd.setText(null);

        
        btnAdicionar.setEnabled(true);
        
        tblProduto.setEnabled(true);
        txtPesqProduto.setEnabled(true);
        
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void limparCampos() {

        txtMarcaProd.setText(null); 
        txtIdProduto.setText(null);
        txtDataProd.setText(null);
        cboStatusProd.setSelectedItem(" ");
        txtProd.setText(null);
        txtDescProd.setText(null);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        cboStatusProd = new javax.swing.JComboBox<>();
        btnPesqMarca = new javax.swing.JLabel();
        txtDescProd = new javax.swing.JTextField();
        btnLimparDados = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        pnlProdutos = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblProduto = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        txtDataProd = new javax.swing.JTextField();
        txtPesqProduto = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtIdProduto = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnAlterar = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtProd = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        btnInativar = new javax.swing.JButton();
        btnAdicionar = new javax.swing.JButton();
        btnAtivar = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        txtMarcaProd = new javax.swing.JTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR PRODUTO");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        cboStatusProd.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "Disponivel", "Esgotado", "Inativo" }));
        cboStatusProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboStatusProdActionPerformed(evt);
            }
        });

        btnPesqMarca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqMarca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqMarcaMouseClicked(evt);
            }
        });

        txtDescProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtDescProdActionPerformed(evt);
            }
        });

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

        jLabel4.setText("* SITUAÇÃO:");

        pnlProdutos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlProdutosMouseClicked(evt);
            }
        });

        tblProduto = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblProduto.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "PRODUTO", "MARCA", "DESCRIÇÃO", "SITUAÇÃO", "DATA", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblProduto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblProdutoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblProduto);

        jLabel6.setText("DATA:");

        txtDataProd.setEditable(false);

        txtPesqProduto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqProdutoKeyReleased(evt);
            }
        });

        jLabel1.setText("ID:");

        txtIdProduto.setEditable(false);

        jLabel7.setText("PRODUTO:");

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel9.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel9.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout pnlProdutosLayout = new javax.swing.GroupLayout(pnlProdutos);
        pnlProdutos.setLayout(pnlProdutosLayout);
        pnlProdutosLayout.setHorizontalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProdutosLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(pnlProdutosLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPesqProduto, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtIdProduto, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDataProd, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        pnlProdutosLayout.setVerticalGroup(
            pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProdutosLayout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlProdutosLayout.createSequentialGroup()
                        .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(txtPesqProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE))
                    .addGroup(pnlProdutosLayout.createSequentialGroup()
                        .addGroup(pnlProdutosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDataProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(txtIdProduto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarEstoque.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Produto");
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        jLabel2.setText("PRODUTO:");

        txtProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtProdActionPerformed(evt);
            }
        });

        jLabel5.setText("* MARCA:");

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Produto");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarProduto.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Produto");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Produto");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        jLabel3.setText("DESCRIÇÃO:");

        txtMarcaProd.setEditable(false);
        txtMarcaProd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtMarcaProdActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 139, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(69, 69, 69)
                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(68, 68, 68)
                        .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(60, 60, 60)
                        .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel3)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtDescProd, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(33, 33, 33)
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtMarcaProd, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnPesqMarca))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(txtProd, javax.swing.GroupLayout.PREFERRED_SIZE, 559, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(109, 109, 109))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cboStatusProd, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(147, 147, 147))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlProdutos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cboStatusProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(txtDescProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(txtMarcaProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnPesqMarca))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39))
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 837, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
        );

        setBounds(0, 0, 853, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void cboStatusProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboStatusProdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboStatusProdActionPerformed

    private void btnPesqMarcaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqMarcaMouseClicked
        pesquisarMarca();
    }//GEN-LAST:event_btnPesqMarcaMouseClicked

    private void txtDescProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDescProdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDescProdActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void tblProdutoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblProdutoMouseClicked
        setarCampos();
    }//GEN-LAST:event_tblProdutoMouseClicked

    private void txtPesqProdutoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqProdutoKeyReleased
        pesquisarProduto();
    }//GEN-LAST:event_txtPesqProdutoKeyReleased

    private void pnlProdutosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlProdutosMouseClicked

        tblProduto.clearSelection();
        limpezaGeralTbl();
    }//GEN-LAST:event_pnlProdutosMouseClicked

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterarProduto();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void txtProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtProdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtProdActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        inativarProduto();
    }//GEN-LAST:event_btnInativarActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarProduto();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarProduto();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void txtMarcaProdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtMarcaProdActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtMarcaProdActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel btnPesqMarca;
    private javax.swing.JComboBox<String> cboStatusProd;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlProdutos;
    private javax.swing.JTable tblProduto;
    private javax.swing.JTextField txtDataProd;
    private javax.swing.JTextField txtDescProd;
    private javax.swing.JTextField txtIdProduto;
    private javax.swing.JTextField txtMarcaProd;
    private javax.swing.JTextField txtPesqProduto;
    private javax.swing.JTextField txtProd;
    // End of variables declaration//GEN-END:variables
}

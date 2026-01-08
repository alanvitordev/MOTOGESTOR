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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;


public class TelaServico extends javax.swing.JInternalFrame {

    
    Connection conexao = null; 
    PreparedStatement pst = null; 
    ResultSet rs = null;
    private int idUser;

   
    public TelaServico(int idUser) {
        initComponents();

        //CONEXAO SQL
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarServico();
    }

    private void adicionarServico() {

        String sql_valida = "SELECT 1 FROM tbservicos WHERE servico = ?";

        String sql_adicionar = "insert into tbservicos (servico, valor, status, idusuario) values (?, ?, 'Ativo', ?)";

        try {

            String servicoStr = txtServico.getText().trim();
            String valorStr = txtValorServico.getText().trim().replace(",", ".");

            if (servicoStr.isEmpty() || valorStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return;
            }

            double valor;

            try {
                valor = Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Erro: Valor do serviço é inválido.");
                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, servicoStr); // Checa o nome do serviço
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Um serviço com este nome já está cadastrado!");
                return; // Para o método
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, servicoStr);
            pst.setDouble(2, valor);
            pst.setInt(3, TelaPrincipal.idUser);

            int adicionado = pst.executeUpdate();
            
            if (adicionado > 0) {
                
                JOptionPane.showMessageDialog(null, "Serviço adicionado com sucesso!");
                
                limparCampos();
                logicaInsert();
                pesquisarServico();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            //fecharConexao(); 
        }
    }

    private void alterarServico() {

        String sql_valida = "SELECT 1 FROM tbservicos WHERE servico = ? AND idservico <> ?";

        String sql_alterar = "update tbservicos set servico=?, valor=?, idusuario=?, data = now() where idservico=?";

        try {

            String servicoStr = txtServico.getText().trim();
            String valorStr = txtValorServico.getText().trim().replace(",", ".");
            String idServicoStr = txtIdServico.getText().trim();

            if (idServicoStr.isEmpty() || servicoStr.isEmpty() || valorStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Preencha os campos obrigatórios!");
                return;
            }

            int idServico;
            double valor;
            try {
                idServico = Integer.parseInt(idServicoStr);
                valor = Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Erro: ID ou Valor do serviço é inválido.");
                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, servicoStr); // Checa o nome
            pst_valida.setInt(2, idServico);     // Ignora o ID atual
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {

                JOptionPane.showMessageDialog(null, "Atenção: Este nome de serviço já está em uso!");

                return; // Para o método
            }

            pst = conexao.prepareStatement(sql_alterar);
            pst.setString(1, servicoStr);
            pst.setDouble(2, valor);
            pst.setInt(3, TelaPrincipal.idUser);
            pst.setInt(4, idServico);

            int alterado = pst.executeUpdate();

            if (alterado > 0) {

                JOptionPane.showMessageDialog(null, "Serviço alterado com sucesso!");

                limparCampos();
                logicaUpdate();
                pesquisarServico();

            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        } finally {
            //fecharConexao(); 
        }
    }

    // método para pesquisar serviços pelo nome e listar na tabela
    private void pesquisarServico() {

        String sql = "select idservico as ID, servico as SERVIÇO, valor as VALOR, "
                + "date_format(data,'%d/%m/%Y - %H:%i') as DATA, "
                + "CASE WHEN status = TRUE THEN 'Ativo' ELSE 'Inativo' END as STATUS "
                + "from tbservicos where servico like ?";

        try {
            pst = conexao.prepareStatement(sql);

            pst.setString(1, "%" + txtPesqFornecedor.getText().trim() + "%");

            rs = pst.executeQuery();
            // Popular a tela com os dados da tabela

            tblServicos.setModel(net.proteanit.sql.DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        } finally {

        }
    }

    private void preencherTabela() {

        try {
            int setar = tblServicos.getSelectedRow();

            txtIdServico.setText(tblServicos.getModel().getValueAt(setar, 0).toString());   // idservico
            txtServico.setText(tblServicos.getModel().getValueAt(setar, 1).toString());     // servico

            txtValorServico.setText(tblServicos.getModel().getValueAt(setar, 2).toString());// valor

            String status = tblServicos.getModel().getValueAt(setar, 4).toString();
            
            logicaTbl();

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao atualizar os dados na tela: " + e);

        } finally {
            
        }
    }

    private void inativarServico() {

        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja INATIVAR este serviço?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_NO_OPTION) {
            
            String sql = "update tbservicos set status = 'Inativo', idusuario=? where idservico =?";

            try {
                
                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdServico.getText());

                int inativado = pst.executeUpdate();
                
                if (inativado > 0) {

                    JOptionPane.showMessageDialog(null, "Serviço inativado com sucesso!");

                    limparCampos();
                    logicaInativar();
                    pesquisarServico();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);

            } finally {
            }
        }
    }

    private void ativarServico() {

        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja ATIVAR este serviço?", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_NO_OPTION) {
            String sql = "update tbservicos set status = 'Ativo', idusuario=? where idservico =?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdServico.getText());

                int ativado = pst.executeUpdate();

                if (ativado >= 1) {

                    JOptionPane.showMessageDialog(null, "Serviço ativado com sucesso!");
                    
                    limparCampos();
                    logicaAtivar();
                    pesquisarServico();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);

            } finally {
     
            }
        }
    }

    private void logicaInsert() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
        tblServicos.setEnabled(true);
        txtPesqFornecedor.setEnabled(true);
       

    }

    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblServicos.setEnabled(true);
        txtPesqFornecedor.setEnabled(true);
        
     
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
        tblServicos.setEnabled(true);
        txtPesqFornecedor.setEnabled(true);
        
    }
    
    
    private void logicaInativar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblServicos.setEnabled(true);
        txtPesqFornecedor.setEnabled(true);
        
    }
    
     private void limpezaGeralTbl() { 
        
        txtIdServico.setText(null);
        txtPesqFornecedor.setText(null);
        txtServico.setText(null);
        txtValorServico.setText(null);

        
        btnAdicionar.setEnabled(true);
        tblServicos.setEnabled(true);
        txtPesqFornecedor.setEnabled(true);
        
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

     }
     
    private void limparCampos() {
        
        txtIdServico.setText(null);
        txtPesqFornecedor.setText(null);
        txtServico.setText(null);
        //txtDescricaoServ.setText(null);
        txtValorServico.setText(null);
        //txtColaboradorServ.setText(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        btnInativar = new javax.swing.JButton();
        txtValorServico = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        pnlServicos = new javax.swing.JPanel();
        txtPesqFornecedor = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtIdServico = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblServicos = new javax.swing.JTable();
        jLabel3 = new javax.swing.JLabel();
        btnLimparDados = new javax.swing.JButton();
        txtServico = new javax.swing.JTextField();
        btnAtivar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR SERVIÇO");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarDocumento.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Serviço");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarDocumento.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Serviço");
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Serviço");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        jLabel4.setText("* VALOR:");

        jLabel2.setText("* SERVIÇO:");

        pnlServicos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlServicos.setToolTipText("Serviço");
        pnlServicos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlServicosMouseClicked(evt);
            }
        });

        txtPesqFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesqFornecedorActionPerformed(evt);
            }
        });
        txtPesqFornecedor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqFornecedorKeyReleased(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel1.setText("ID:");

        txtIdServico.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdServico.setEnabled(false);
        txtIdServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdServicoActionPerformed(evt);
            }
        });

        tblServicos = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblServicos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "SERVIÇO", "DESCRIÇÃO", "VALOR", "DATA", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblServicos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblServicosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblServicos);

        jLabel3.setText("SERVIÇO:");

        javax.swing.GroupLayout pnlServicosLayout = new javax.swing.GroupLayout(pnlServicos);
        pnlServicos.setLayout(pnlServicosLayout);
        pnlServicosLayout.setHorizontalGroup(
            pnlServicosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServicosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdServico, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
            .addGroup(pnlServicosLayout.createSequentialGroup()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        pnlServicosLayout.setVerticalGroup(
            pnlServicosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlServicosLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(pnlServicosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPesqFornecedor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txtIdServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 248, Short.MAX_VALUE))
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

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Serviço");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValorServico, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtServico, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(141, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(43, 43, 43)
                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlServicos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlServicos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtValorServico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
        );

        jScrollPane3.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarServico();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterarServico();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        inativarServico();        // TODO add your handling code here:
    }//GEN-LAST:event_btnInativarActionPerformed

    private void txtPesqFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqFornecedorActionPerformed
        //ADICIONEI O EVENTO ERRO PARA O CAMPO DE TEXTO "PESQUISA".
    }//GEN-LAST:event_txtPesqFornecedorActionPerformed

    private void txtPesqFornecedorKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqFornecedorKeyReleased
        //chamando o método PesquisarCliente();

        pesquisarServico();
    }//GEN-LAST:event_txtPesqFornecedorKeyReleased

    private void txtIdServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdServicoActionPerformed
        //ADICIONEI UM EVENTO SEM QUERER AQUI A CAIXA DE TEXTO EM FRENTE AO "NOME:"
    }//GEN-LAST:event_txtIdServicoActionPerformed

    private void tblServicosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblServicosMouseClicked
        preencherTabela();
        // TODO add your handling code here:
    }//GEN-LAST:event_tblServicosMouseClicked

    private void pnlServicosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlServicosMouseClicked

        tblServicos.clearSelection();

        limpezaGeralTbl();
    }//GEN-LAST:event_pnlServicosMouseClicked

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarServico();
    }//GEN-LAST:event_btnAtivarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel pnlServicos;
    private javax.swing.JTable tblServicos;
    private javax.swing.JTextField txtIdServico;
    private javax.swing.JTextField txtPesqFornecedor;
    private javax.swing.JTextField txtServico;
    private javax.swing.JTextField txtValorServico;
    // End of variables declaration//GEN-END:variables
}

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

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class TelaMecanico extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private int idUser;
    
    public TelaMecanico(int idUser) {
        initComponents();

        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarMecanico();
    }

    private void adicionarMecanico() {

        String sql_valida = "SELECT 1 FROM tbmecanico WHERE mecanico = ?";

        String sql_adicionar = "insert into tbmecanico (mecanico, idusuario, status) values (?, ?, 'Ativo')";

        try {

            String mecanico = txtMecanico.getText().toString().trim();

            if (mecanico.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, mecanico); // Checa a descrição
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {

                JOptionPane.showMessageDialog(null, "Atenção: Existe um registro utilizando este nome, tente outro!!");

                return;
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, mecanico);
            pst.setInt(2, TelaPrincipal.idUser);

            int incluida = pst.executeUpdate();

            if (incluida > 0) {

                JOptionPane.showMessageDialog(null, "Mecânico adicionado com sucesso!");
                
                limpezaDados();
     
                pesquisarMecanico();

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void pesquisarMecanico() {

        String sql = "SELECT id_meca as ID, mecanico as MECÂNICO, status as STATUS "
                + "FROM tbmecanico WHERE mecanico LIKE ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtPesqMeca.getText() + "%");
            rs = pst.executeQuery();

            tblMecanico.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void preencherCamposMecanico() {

        int preencher = tblMecanico.getSelectedRow();

        //método para setar as tabelas do formulário.
        txtIdMeca.setText(tblMecanico.getModel().getValueAt(preencher, 0).toString());
        txtMecanico.setText(tblMecanico.getModel().getValueAt(preencher, 1).toString());
        
        logicaTbl();

    }

    private void atualizarMecanico() {

        String sql_valida = "SELECT 1 FROM tbmecanico WHERE mecanico = ? AND id_meca <> ?";

        String sql = "update tbmecanico set mecanico =?, idusuario =?, data = now() where id_meca = ?";

        try {

            String mecanico = txtMecanico.getText().toString().trim();
            String idMeca = txtIdMeca.getText().trim();

            if (mecanico.isEmpty() || idMeca.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return; 
            }

            int idMecanico;

            try {

                idMecanico = Integer.parseInt(idMeca);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do mecânico está inválido.");
                return;
            }
            
            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, mecanico); 

            pst_valida.setInt(2, idMecanico);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                JOptionPane.showMessageDialog(null, "Atenção: Esta descrição já está cadastrada em outra marca!");
                return; 
            }

            pst = conexao.prepareStatement(sql);

            pst.setString(1, mecanico);
            pst.setInt(2, TelaPrincipal.idUser);
            pst.setInt(3, idMecanico);

            int mecaAdd = pst.executeUpdate(); 
            
            if (mecaAdd > 0) {

                JOptionPane.showMessageDialog(null, "Colaborador alterado com sucesso!");

                limpezaDados();
                logicaUpdate();
                pesquisarMecanico();

            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }
    
     private void inativarMecanico() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja INATIVAR este colaborador?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmecanico SET status = 'Inativo', idusuario=? WHERE id_meca = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMeca.getText()); // pega o número do ID

                int inativado = pst.executeUpdate();

                if (inativado > 0) {
                    
                    JOptionPane.showMessageDialog(null, "Colaborador inativado com sucesso!");

                    limpezaDados();
                    logicaInativar();
                    pesquisarMecanico();
                    
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarMecanico() {

        int ativado = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja ATIVAR este colaborador?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (ativado == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmecanico SET status = 'Ativo', idusuario=? WHERE id_meca = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMeca.getText()); // pega o número do ID

                int ativada = pst.executeUpdate();

                if (ativada >= 1) {
                    
                    JOptionPane.showMessageDialog(null, "Colaborador ativado com sucesso!");

                    limpezaDados();
                    logicaAtivar();
                    pesquisarMecanico();
                    
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
        tblMecanico.setEnabled(true);
        txtPesqMeca.setEnabled(true);
        
    }
    
     private void logicaTbl () {
        
        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);
        
        btnAdicionar.setEnabled(false);
        
    }
    
    private void logicaAtivar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblMecanico.setEnabled(true);
        txtPesqMeca.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
    }
    
    private void logicaInativar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblMecanico.setEnabled(true);
        txtPesqMeca.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        
        
    }
    
     private void limpezaGeralTbl() {
        
        txtMecanico.setText(null);
        txtIdMeca.setText(null);
        txtPesqMeca.setText(null);
        
        btnAdicionar.setEnabled(true);
        
        tblMecanico.setEnabled(true);
        txtPesqMeca.setEnabled(true);
        
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);


    }
    

    private void limpezaDados() { 

        txtMecanico.setText(null);
        txtIdMeca.setText(null);
        txtPesqMeca.setText(null);
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        btnInativar = new javax.swing.JButton();
        btnAtivar = new javax.swing.JButton();
        btnLimparDados = new javax.swing.JButton();
        txtMecanico = new javax.swing.JTextField();
        btnAlterar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        btnAdicionar = new javax.swing.JButton();
        pnlMecanico = new javax.swing.JPanel();
        txtIdMeca = new javax.swing.JTextField();
        txtPesqMeca = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMecanico = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR MECÂNICO");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Cliente");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Cliente");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
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

        jLabel1.setText("* MECÂNICO:");

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarPessoas.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Mecânico");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        pnlMecanico.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlMecanico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlMecanicoMouseClicked(evt);
            }
        });

        txtIdMeca.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdMeca.setEnabled(false);
        txtIdMeca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMecaActionPerformed(evt);
            }
        });

        txtPesqMeca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesqMecaActionPerformed(evt);
            }
        });
        txtPesqMeca.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqMecaKeyReleased(evt);
            }
        });

        tblMecanico = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;

            }
        };
        tblMecanico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "MECÂNICO", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMecanico.setFocusable(false);
        tblMecanico.getTableHeader().setReorderingAllowed(false);
        tblMecanico.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMecanicoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMecanico);

        jLabel7.setText("ID:");

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel9.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel9.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel2.setText("MECÂNICO:");

        javax.swing.GroupLayout pnlMecanicoLayout = new javax.swing.GroupLayout(pnlMecanico);
        pnlMecanico.setLayout(pnlMecanicoLayout);
        pnlMecanicoLayout.setHorizontalGroup(
            pnlMecanicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMecanicoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqMeca, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdMeca, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        pnlMecanicoLayout.setVerticalGroup(
            pnlMecanicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMecanicoLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(pnlMecanicoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPesqMeca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtIdMeca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(75, 75, 75)
                .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(71, 71, 71)
                .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 84, Short.MAX_VALUE)
                .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlMecanico, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMecanico, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(27, 27, 27))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(pnlMecanico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtMecanico, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(62, 62, 62)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        //chamando o método InativarCliente();

        inativarMecanico();
    }//GEN-LAST:event_btnInativarActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarMecanico();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limpezaDados();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        atualizarMecanico();        // TODO add your handling code here:
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarMecanico();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void txtIdMecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMecaActionPerformed

    }//GEN-LAST:event_txtIdMecaActionPerformed

    private void txtPesqMecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqMecaActionPerformed

    }//GEN-LAST:event_txtPesqMecaActionPerformed

    private void txtPesqMecaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqMecaKeyReleased

        pesquisarMecanico();
    }//GEN-LAST:event_txtPesqMecaKeyReleased

    private void tblMecanicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMecanicoMouseClicked

        preencherCamposMecanico();
    }//GEN-LAST:event_tblMecanicoMouseClicked

    private void pnlMecanicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMecanicoMouseClicked

        tblMecanico.clearSelection();

        limpezaGeralTbl();
    }//GEN-LAST:event_pnlMecanicoMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlMecanico;
    private javax.swing.JTable tblMecanico;
    private javax.swing.JTextField txtIdMeca;
    private javax.swing.JTextField txtMecanico;
    private javax.swing.JTextField txtPesqMeca;
    // End of variables declaration//GEN-END:variables
}

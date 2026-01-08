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
import br.com.motogestor.telas.SubTelaPesqProduto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class TelaEstoque extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private int idUser;

    public TelaEstoque(int idUser) {
        initComponents();
        
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        listarEstoque();

    }

    public void listarEstoque() {
        String sql = "SELECT est.id_movimentacao, est.codproduto, pro.produto, est.quantidade, est.dtatualizacao FROM tbestoque est inner join tbprodutos pro on (pro.idproduto = est.codproduto)";

        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) tblMovimentacao.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                
                model.addRow(new Object[]{
                    
                    rs.getInt("id_movimentacao"),
                    rs.getInt("codproduto"),
                    rs.getString("produto"),
                    rs.getDouble("quantidade"),
                    rs.getTimestamp("dtatualizacao")
                        
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar estoque: " + e.getMessage());
        }
    }

    private void selecionarEstoque() {

        int setar = tblMovimentacao.getSelectedRow(); 

        if (setar >= 0) {
            
            txtIdMov.setText(tblMovimentacao.getModel().getValueAt(setar, 0).toString());
            txtProd.setText(tblMovimentacao.getModel().getValueAt(setar, 1).toString() + " - " + tblMovimentacao.getModel().getValueAt(setar, 2).toString());
            txtQtd.setText(tblMovimentacao.getModel().getValueAt(setar, 3).toString());
        }
    }

    private void atualizarEstoque() {

        String sql = "UPDATE tbestoque SET quantidade = ?, idusuario = ? WHERE id_movimentacao = ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setDouble(1, Double.parseDouble(txtQtd.getText().trim()));
            pst.setInt(2, TelaPrincipal.idUser);
            pst.setInt(3, Integer.parseInt(txtIdMov.getText().trim()));
           

            if (txtQtd.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Informe a quantidade!");
                return;
            }

            int atualizado = pst.executeUpdate();

            if (atualizado > 0) {
                JOptionPane.showMessageDialog(null, "Estoque atualizado com sucesso!");

                listarEstoque();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao atualizar estoque: " + e.getMessage());
        }
    }

    public void pesquisarEstoque() {
        
        String sql = "SELECT est.id_movimentacao, est.codproduto, pro.produto, est.quantidade, est.dtatualizacao "
                + "FROM tbestoque est "
                + "INNER JOIN tbprodutos pro ON (pro.idproduto = est.codproduto) "
                + "WHERE pro.produto LIKE ?";

        try {
            
            pst = conexao.prepareStatement(sql);
            pst.setString(1, "%" + txtPesqProd.getText().trim() + "%");
            rs = pst.executeQuery();

            DefaultTableModel model = (DefaultTableModel) tblMovimentacao.getModel();
            model.setRowCount(0);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_movimentacao"),
                    rs.getInt("codproduto"),
                    rs.getString("produto"),
                    rs.getDouble("quantidade"),
                    rs.getTimestamp("dtatualizacao")
                        
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao pesquisar estoque: " + e.getMessage());
        }
    }

    private void limparCampos() {

        // limpa os campos
        txtIdMov.setText(null);
        txtProd.setText(null);
        txtQtd.setText(null);
        ((DefaultTableModel) tblMovimentacao.getModel()).setRowCount(0);

    }
    
   private void limparCamposTbl() {
       
       txtIdMov.setText(null);
        txtProd.setText(null);
        txtQtd.setText(null);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMovimentacao = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtProd = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtQtd = new javax.swing.JTextField();
        btnAlterarMov = new javax.swing.JButton();
        btnLimparDados = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtPesqProd = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtIdMov = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - ESTOQUE");
        setPreferredSize(new java.awt.Dimension(850, 600));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setPreferredSize(new java.awt.Dimension(730, 553));

        tblMovimentacao = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblMovimentacao.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID Mov.", "Cód. Produto", "Produto", "Quantidade", "Data"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMovimentacao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMovimentacaoMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMovimentacao);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 271, Short.MAX_VALUE)
        );

        jLabel1.setText("PRODUTO:");

        txtProd.setEnabled(false);

        jLabel4.setText("* QUANTIDADE:");

        btnAlterarMov.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarEstoque.png"))); // NOI18N
        btnAlterarMov.setText("Atualizar estoque");
        btnAlterarMov.setPreferredSize(new java.awt.Dimension(60, 60));
        btnAlterarMov.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarMovActionPerformed(evt);
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

        jLabel2.setText("PRODUTO:");

        txtPesqProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqProdKeyReleased(evt);
            }
        });

        jLabel3.setText("ID. Mov.:");

        txtIdMov.setEnabled(false);
        txtIdMov.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMovActionPerformed(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 826, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtProd)
                        .addGap(76, 76, 76))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtQtd, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPesqProd, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtIdMov, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18))))
            .addGroup(layout.createSequentialGroup()
                .addGap(283, 283, 283)
                .addComponent(btnAlterarMov, javax.swing.GroupLayout.PREFERRED_SIZE, 255, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(txtIdMov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(txtPesqProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(14, 14, 14)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtQtd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(btnAlterarMov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAlterarMovActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarMovActionPerformed
        atualizarEstoque();
    }//GEN-LAST:event_btnAlterarMovActionPerformed

    private void tblMovimentacaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMovimentacaoMouseClicked
        selecionarEstoque();
    }//GEN-LAST:event_tblMovimentacaoMouseClicked

    private void txtIdMovActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMovActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdMovActionPerformed

    private void txtPesqProdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqProdKeyReleased
        // Aqui vai pesquisar o estoque
        pesquisarEstoque();
    }//GEN-LAST:event_txtPesqProdKeyReleased

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
          limparCamposTbl();
   
    tblMovimentacao.clearSelection();
    }//GEN-LAST:event_formMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAlterarMov;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblMovimentacao;
    private javax.swing.JTextField txtIdMov;
    private javax.swing.JTextField txtPesqProd;
    private javax.swing.JTextField txtProd;
    private javax.swing.JTextField txtQtd;
    // End of variables declaration//GEN-END:variables
}

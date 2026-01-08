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

public class TelaPrecificacao extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    private int idUser;

    public TelaPrecificacao(int idUser) {
        initComponents();
        
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        listarProdutos();

    }

    public void listarProdutos() {

        String sql = "select prec.id_movimentacao, prec.codproduto, pro.produto, prec.valorcusto, prec.margemlucro, prec.valorvenda, prec.dtatualizacao from tbprecificacao prec "
                + " inner join tbprodutos pro on (prec.codproduto = pro.idproduto)";

        try {
            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();

            // Modelo da tabela
            DefaultTableModel model = (DefaultTableModel) tblMovimentacao.getModel();
            model.setRowCount(0); // limpar tabela

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_movimentacao"),
                    rs.getInt("codproduto"),
                    rs.getString("produto"),
                    rs.getDouble("valorcusto"),
                    rs.getDouble("margemlucro"),
                    rs.getDouble("valorvenda"),
                    rs.getTimestamp("dtatualizacao")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erro ao listar estoque: " + e.getMessage());
        }
    }

    private void selecionarProdutos() {

        int setar = tblMovimentacao.getSelectedRow(); // linha selecionada

        if (setar >= 0) {
            txtIdMov.setText(tblMovimentacao.getModel().getValueAt(setar, 0).toString());
            txtProd.setText(tblMovimentacao.getModel().getValueAt(setar, 1).toString() + " - " + tblMovimentacao.getModel().getValueAt(setar, 2).toString());
            txtVlrCus.setText(tblMovimentacao.getModel().getValueAt(setar, 3).toString());
            txtMargemLucro.setText(tblMovimentacao.getModel().getValueAt(setar, 4).toString());
            txtVlrVenda.setText(tblMovimentacao.getModel().getValueAt(setar, 5).toString());
        }
    }

    public void pesquisarProdutos() {

        String sql = "select prec.id_movimentacao, prec.codproduto, pro.produto, prec.valorcusto, prec.margemlucro, "
                + " prec.valorvenda, prec.dtatualizacao from tbprecificacao prec " + " inner join tbprodutos pro on (prec.codproduto = pro.idproduto) " + " where pro.produto like ?";

        try {

            pst = conexao.prepareStatement(sql);
            pst.setString(1, "%" + txtPesqProd.getText().trim() + "%");
            rs = pst.executeQuery();
            DefaultTableModel model = (DefaultTableModel) tblMovimentacao.getModel();

            model.setRowCount(0);

            while (rs.next()) {

                model.addRow(new Object[]{rs.getInt("id_movimentacao"), rs.getInt("codproduto"), rs.getString("produto"), rs.getDouble("valorcusto"), rs.getDouble("margemlucro"), rs.getDouble("valorvenda"), rs.getTimestamp("dtatualizacao")});

            }
        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar estoque: " + e.getMessage());
        }
    }

    private void calcularVendaOuMargem() {

        try {
            double custo = Double.parseDouble(txtVlrCus.getText().replace(",", "."));
            String margemStr = txtMargemLucro.getText().trim();
            String vendaStr = txtVlrVenda.getText().trim();

            if (!margemStr.isEmpty() && (vendaStr.isEmpty() || txtMargemLucro.isFocusOwner())) {
                // Cálculo de venda a partir da margem
                double margem = Double.parseDouble(margemStr.replace(",", "."));
                double venda = custo * (1 + margem / 100);
                txtVlrVenda.setText(String.format("%.2f", venda));
            } else if (!vendaStr.isEmpty() && txtVlrVenda.isFocusOwner()) {
                // Cálculo da margem a partir da venda
                double venda = Double.parseDouble(vendaStr.replace(",", "."));
                double margem = ((venda / custo) - 1) * 100;
                txtMargemLucro.setText(String.format("%.2f", margem));
            }

        } catch (Exception e) {
            // Silencioso enquanto usuário digita
        }
    }

    private void atualizarPrecificacao() {

        String sql = "UPDATE tbprecificacao SET valorcusto=?, margemlucro=?, valorvenda=?, idusuario=? WHERE id_movimentacao=?";

        try {

            pst = conexao.prepareStatement(sql);
            pst.setDouble(1, Double.parseDouble(txtVlrCus.getText().replace(",", ".")));
            pst.setDouble(2, Double.parseDouble(txtMargemLucro.getText().replace(",", ".")));
            pst.setDouble(3, Double.parseDouble(txtVlrVenda.getText().replace(",", ".")));
            pst.setInt(4, TelaPrincipal.idUser);
            pst.setInt(5, Integer.parseInt(txtIdMov.getText()));

            pst.executeUpdate();
            JOptionPane.showMessageDialog(null, "Preço atualizado com sucesso!");
            pesquisarProdutos(); // ou listar

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao atualizar: " + e.getMessage());
        }
    }

    private void limparCampos() {

        // limpa os campos
        txtIdMov.setText(null);
        txtProd.setText(null);
        txtVlrCus.setText(null);
        txtMargemLucro.setText(null);
        txtVlrVenda.setText(null);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtProd = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtVlrCus = new javax.swing.JTextField();
        btnAlterarMov = new javax.swing.JButton();
        btnLimparDados = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtMargemLucro = new javax.swing.JTextField();
        txtvenda = new javax.swing.JLabel();
        txtVlrVenda = new javax.swing.JTextField();
        pnlPrecificacao = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        txtPesqProd = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        txtIdMov = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMovimentacao = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - PRECIFICAÇÃO PRODUTO");
        setPreferredSize(new java.awt.Dimension(850, 600));

        txtProd.setEnabled(false);

        jLabel4.setText("* VALOR CUSTO:");

        txtVlrCus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtVlrCusKeyReleased(evt);
            }
        });

        btnAlterarMov.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarEstoque.png"))); // NOI18N
        btnAlterarMov.setText("Atualizar Preço");
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

        jLabel5.setText("* MARGEM LUCRO:");

        txtMargemLucro.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtMargemLucroKeyReleased(evt);
            }
        });

        txtvenda.setText("* VALOR VENDA:");

        txtVlrVenda.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtVlrVendaKeyReleased(evt);
            }
        });

        pnlPrecificacao.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlPrecificacao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlPrecificacaoMouseClicked(evt);
            }
        });

        jLabel3.setText("ID. Mov.:");

        txtPesqProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqProdKeyReleased(evt);
            }
        });

        jLabel1.setText("PRODUTO:");

        txtIdMov.setEnabled(false);
        txtIdMov.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMovActionPerformed(evt);
            }
        });

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
                "ID Mov.", "Cód. Produto", "Produto", "Vlr. Custo", "Margem Lucro", "Vlr. Venda", "Data"
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

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout pnlPrecificacaoLayout = new javax.swing.GroupLayout(pnlPrecificacao);
        pnlPrecificacao.setLayout(pnlPrecificacaoLayout);
        pnlPrecificacaoLayout.setHorizontalGroup(
            pnlPrecificacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrecificacaoLayout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqProd, javax.swing.GroupLayout.PREFERRED_SIZE, 346, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdMov, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
            .addComponent(jScrollPane1)
        );
        pnlPrecificacaoLayout.setVerticalGroup(
            pnlPrecificacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPrecificacaoLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlPrecificacaoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPesqProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txtIdMov, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtProd)
                        .addGap(18, 18, 18)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtVlrCus, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(94, 94, 94)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtMargemLucro, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 68, Short.MAX_VALUE)
                        .addComponent(txtvenda)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtVlrVenda, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(51, 51, 51))))
            .addComponent(pnlPrecificacao, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAlterarMov, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(300, 300, 300))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnlPrecificacao, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtvenda)
                        .addComponent(txtVlrVenda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(txtMargemLucro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel4)
                        .addComponent(txtVlrCus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addComponent(btnAlterarMov, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(21, 21, 21))
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAlterarMovActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarMovActionPerformed
        atualizarPrecificacao();
    }//GEN-LAST:event_btnAlterarMovActionPerformed

    private void txtVlrCusKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVlrCusKeyReleased
        // TODO add your handling code here:
        calcularVendaOuMargem();
    }//GEN-LAST:event_txtVlrCusKeyReleased

    private void txtMargemLucroKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMargemLucroKeyReleased
        // TODO add your handling code here:
        calcularVendaOuMargem();
    }//GEN-LAST:event_txtMargemLucroKeyReleased

    private void txtVlrVendaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtVlrVendaKeyReleased
        // TODO add your handling code here:
        calcularVendaOuMargem();
    }//GEN-LAST:event_txtVlrVendaKeyReleased

    private void txtPesqProdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqProdKeyReleased
        // Aqui vai pesquisar o estoque
        pesquisarProdutos();
    }//GEN-LAST:event_txtPesqProdKeyReleased

    private void txtIdMovActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMovActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdMovActionPerformed

    private void tblMovimentacaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMovimentacaoMouseClicked
        selecionarProdutos();
    }//GEN-LAST:event_tblMovimentacaoMouseClicked

    private void pnlPrecificacaoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlPrecificacaoMouseClicked
        limparCampos();
   
    tblMovimentacao.clearSelection();
    }//GEN-LAST:event_pnlPrecificacaoMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAlterarMov;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlPrecificacao;
    private javax.swing.JTable tblMovimentacao;
    private javax.swing.JTextField txtIdMov;
    private javax.swing.JTextField txtMargemLucro;
    private javax.swing.JTextField txtPesqProd;
    private javax.swing.JTextField txtProd;
    private javax.swing.JTextField txtVlrCus;
    private javax.swing.JTextField txtVlrVenda;
    private javax.swing.JLabel txtvenda;
    // End of variables declaration//GEN-END:variables
}

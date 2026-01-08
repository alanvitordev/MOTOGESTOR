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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.motogestor.DAL.ModuloConexao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class SubTelaPesqMecanico extends javax.swing.JDialog {

    private TelaOS telaOS = null;
    public String mecanicoSelecionado;

    //PARAMETROS DA QUERY
    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

    public SubTelaPesqMecanico(java.awt.Frame parent, boolean modal) {
        
        this(parent, modal, null); // delega para o outro construtor
    }

    // novo construtor que recebe a tela chamadora (TelaOS)
    
    public SubTelaPesqMecanico(java.awt.Frame parent, boolean modal, TelaOS telaOS) {
        super(parent, modal);
        
        initComponents();
        
        this.telaOS = telaOS;
        conexao = ModuloConexao.conector();
        pesquisarMecanico();
    }

    private void pesquisarMecanico() {

        String sql = "SELECT id_meca as ID, mecanico as MECÂNICO "
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

        if (preencher >= 0) {
            
            String idStr = tblMecanico.getModel().getValueAt(preencher, 0).toString();
            mecanicoSelecionado = tblMecanico.getValueAt(preencher, 1).toString();
             

            // se a subtela recebeu uma referência para TelaOS, passa os valores pra ela
            
            if (this.telaOS != null) {
                
                try {
                    
                    Integer id = Integer.parseInt(idStr);
                    this.telaOS.setMecSelecionado(id, mecanicoSelecionado); // método em TelaOS
                    
                } catch (NumberFormatException ex) {
                    
                    // se parsing falhar, passa null no id (apenas o nome)
                    this.telaOS.setMecSelecionado(null, mecanicoSelecionado);
                }
            } else {
                // comportamento antigo / fallback: preenche os campos locais
                txtIdMeca.setText(idStr);
            }

            dispose(); // fecha a tela

        } else {
            JOptionPane.showMessageDialog(this, "Selecione um mecânico! ");
        }
    }
          
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblMecanico = new javax.swing.JTable();
        txtPesqMeca = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtIdMeca = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PESQUISAR MECÂNICO");

        tblMecanico = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;

            }
        };
        tblMecanico.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "ID", "MECÂNICO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
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

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel1.setText("MECÂNICO:");

        txtIdMeca.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdMeca.setEnabled(false);
        txtIdMeca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMecaActionPerformed(evt);
            }
        });

        jLabel2.setText("ID:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqMeca, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 104, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdMeca, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
            .addGroup(layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(txtPesqMeca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdMeca, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addGap(9, 9, 9)))
                .addGap(9, 9, 9)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 348, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        setBounds(0, 0, 698, 470);
    }// </editor-fold>//GEN-END:initComponents

    private void tblMecanicoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMecanicoMouseClicked
        // o código evento que será usado para setar os campos da tabela ao clicar com o mouse em algum campo.

        //chamando o método para puxar oque foi criado dentro do mesmo.

        preencherCamposMecanico();
    }//GEN-LAST:event_tblMecanicoMouseClicked

    private void txtPesqMecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqMecaActionPerformed

    }//GEN-LAST:event_txtPesqMecaActionPerformed

    private void txtPesqMecaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqMecaKeyReleased

        pesquisarMecanico();
    }//GEN-LAST:event_txtPesqMecaKeyReleased

    private void txtIdMecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMecaActionPerformed

    }//GEN-LAST:event_txtIdMecaActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqMecanico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqMecanico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqMecanico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqMecanico.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SubTelaPesqMecanico dialog = new SubTelaPesqMecanico(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblMecanico;
    private javax.swing.JTextField txtIdMeca;
    private javax.swing.JTextField txtPesqMeca;
    // End of variables declaration//GEN-END:variables

}

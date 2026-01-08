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
import br.com.motogestor.DAL.ModuloConexao;
import javax.swing.JOptionPane;
import net.proteanit.sql.DbUtils;

public class SubTelaPesqOs extends javax.swing.JDialog {
    
    public boolean selecionou = false;
    
    public String osSel, tipoSel, motoSel, placaSel, tecnicoSel, defeitoSel, 
                  dataSel, situacaoSel, valorSel, idcliSel, statusSel;

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;

  
    public SubTelaPesqOs(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        
        conexao = ModuloConexao.conector();
        
        // Já carrega a lista ao abrir
        pesquisarOS();
        
        
    }
    
    private void pesquisarOS() {

        String sql = "SELECT "
                + "o.os as OS, "
                + "o.tipo as TIPO, "
                + "o.motocicleta as MOTOCICLETA, "
                + "o.placa as PLACA, "
                + "o.tecnico as TECNICO, "
                + "o.defeito as DEFEITO, "
                + "(SELECT GROUP_CONCAT(s.servico SEPARATOR ', ') FROM tbos_servicos os_s JOIN tbservicos s ON os_s.cod_servico = s.idservico WHERE os_s.os_id = o.os) AS SERVIÇO, "
                + "(SELECT GROUP_CONCAT(p.produto SEPARATOR ', ') FROM tbos_itens os_i JOIN tbprodutos p ON os_i.cod_produto = p.idproduto WHERE os_i.os_id = o.os) AS PRODUTO, "
                + "DATE_FORMAT(o.data, '%d/%m/%Y - %H:%i') AS DATA_OS, "
                + "o.situacao, "
                + "o.valor, "
                + "o.idcli, "
                + "o.statusos "
                + "FROM tbos o "
                + "WHERE o.os LIKE ? OR o.placa LIKE ? OR o.motocicleta LIKE ?";

        try {
            pst = conexao.prepareStatement(sql);
            
            // Replica o texto de pesquisa para os 3 filtros (OS, Placa ou Moto)
            pst.setString(1, txtPesqOs.getText() + "%");
            pst.setString(2, txtPesqOs.getText() + "%");
            pst.setString(3, txtPesqOs.getText() + "%");

            rs = pst.executeQuery();

            // Popula a tabela automaticamente
            tblOs.setModel(DbUtils.resultSetToTableModel(rs));

            // Oculta as colunas técnicas que o DbUtils trouxe (Situação, Valor, ID, Status)
            esconderColunasTecnicas();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // Método para esconder visualmente as colunas que o usuário não precisa ver
    private void esconderColunasTecnicas() {

        // Oculta as colunas: 
        // 9 (situacao), 10 (valor), 11 (idcli), 12 (statusos)
        int[] colunas = {9, 10, 11, 12};

        for (int i : colunas) {
            tblOs.getColumnModel().getColumn(i).setMinWidth(0);
            tblOs.getColumnModel().getColumn(i).setMaxWidth(0);
            tblOs.getColumnModel().getColumn(i).setWidth(0);
            tblOs.getColumnModel().getColumn(i).setPreferredWidth(0);
        }
    }

    private String pegarValor(int linha, int coluna) {

        Object valor = tblOs.getValueAt(linha, coluna);

        if (valor == null) {

            return ""; // Se for nulo, retorna vazio para não travar

        } else {

            return valor.toString(); // Se tiver valor, retorna o texto
        }
    }

    private void preencherCamposOS() {
        int linha = tblOs.getSelectedRow();

        if (linha >= 0) {

            osSel = pegarValor(linha, 0);
            tipoSel = pegarValor(linha, 1);
            motoSel = pegarValor(linha, 2);
            placaSel = pegarValor(linha, 3);
            tecnicoSel = pegarValor(linha, 4);
            defeitoSel = pegarValor(linha, 5);
            // Pula 6 e 7 (visuais)
            dataSel = pegarValor(linha, 8);

            // Dados Ocultos
            situacaoSel = pegarValor(linha, 9);

            // Tratamento Especial para Valor (se vier vazio, assume "0")
            String valor = pegarValor(linha, 10);
            valorSel = valor.isEmpty() ? "0" : valor;

            idcliSel = pegarValor(linha, 11);
            statusSel = pegarValor(linha, 12);

            selecionou = true;
            dispose();

        } else {

            JOptionPane.showMessageDialog(this, "Selecione uma OS! ");
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtPesqOs = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblOs = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("PESQUISAR OS");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtPesqOs.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtPesqOsKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqOsKeyReleased(evt);
            }
        });

        tblOs = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblOs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "OS", "TIPO", "MOTOCICLETA", "PLACA", "TECNICO", "DEFEITO", "SERVIÇO", "PRODUTO", "DATA_OS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblOs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblOsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblOs);

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel1.setText("OS:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 876, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqOs, javax.swing.GroupLayout.PREFERRED_SIZE, 420, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPesqOs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setBounds(0, 0, 916, 431);
    }// </editor-fold>//GEN-END:initComponents

    private void txtPesqOsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqOsKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesqOsKeyPressed

    private void txtPesqOsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqOsKeyReleased
        pesquisarOS();
    }//GEN-LAST:event_txtPesqOsKeyReleased

    private void tblOsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblOsMouseClicked
      preencherCamposOS();

    }//GEN-LAST:event_tblOsMouseClicked

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
            java.util.logging.Logger.getLogger(SubTelaPesqOs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqOs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqOs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SubTelaPesqOs.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SubTelaPesqOs dialog = new SubTelaPesqOs(new javax.swing.JFrame(), true);
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
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblOs;
    private javax.swing.JTextField txtPesqOs;
    // End of variables declaration//GEN-END:variables
}

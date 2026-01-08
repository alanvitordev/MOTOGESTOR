package br.com.motogestor.telas;

import java.sql.*; //importando para fazar a manipulação de dados que estão dentro do banco
import br.com.motogestor.DAL.ModuloConexao; //importando o ModuloConexao que está dentro do pacote br.com.infox.DAL
import br.com.motogestor.util.CriptografiaUtil;
import java.awt.Color;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class TelaLogin extends javax.swing.JFrame {

    Connection conexao = null;
    PreparedStatement pst = null; 
    ResultSet rs = null; 
    
    char senhaPadrao;

    public void logar() {

       
        String sql = "select * from tbusuarios where login = ? and senha = ?";
       
        try {
           

            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtUsuario.getText()); 
            
            String captura = new String(txtSenha.getPassword());
            
            // Aqui o Java pega "123", transforma em "A5F8..."
            String senhaCriptografada = CriptografiaUtil.criptografar(captura);
            // E agora manda "A5F8..." pro banco.
            pst.setString(2, senhaCriptografada);
            
                 
            rs = pst.executeQuery(); 
            
            
            if (rs.next()) { 

                int idUsuario = rs.getInt("idusuario");         // pega id do usuário
                String nomeUsuario = rs.getString("usuario"); // pega nome do usuário
                String perfil = rs.getString("perfil");
                         

               
                if (perfil.equals("ADMIN")) { 

                    //chamando uma classe e instaciando um objeto da mesma, para manipulação.
                    //classe      //objeto
                    TelaPrincipal principal = new TelaPrincipal(idUsuario, nomeUsuario);
                    principal.setVisible(true);
                    
                    // Garante que a janela não esteja minimizada (estado normal)
                    principal.setExtendedState(JFrame.NORMAL);

                    // Traz a janela para a frente de todas as outras janelas
                    principal.toFront();

                    // Pede o foco do sistema operacional para esta janela
                    principal.requestFocus();

                    TelaPrincipal.menRel.setEnabled(true); 

                    TelaPrincipal.menCadUso.setEnabled(true);   
                    
                    TelaPrincipal.menCadCli.setEnabled(true);

                    TelaPrincipal.menCadMoto.setEnabled(true);

                    TelaPrincipal.menCadFornecedor.setEnabled(true);

                    TelaPrincipal.menCadProduto.setEnabled(true);

                    TelaPrincipal.menCadEstoque.setEnabled(true);

                    TelaPrincipal.menCadServico.setEnabled(true);

                    TelaPrincipal.menCadMecanico.setEnabled(true);

                    TelaPrincipal.menCadMarca.setEnabled(true);

                    TelaPrincipal.lblUsuario.setText(rs.getString(2));
                    // e na tela principal aparecerá o nome correspondente do usuário...

                    TelaPrincipal.lblUsuario.setForeground(Color.red); 

                    this.dispose(); 

                    conexao.close();

                } else {

                    TelaPrincipal principal = new TelaPrincipal(idUsuario, nomeUsuario); // acesso a tela principal...
                    principal.setVisible(true); 
                    TelaPrincipal.lblUsuario.setText(rs.getString(2)); 
                    
                    principal.toFront();

                    principal.requestFocus();
                    
                    TelaPrincipal.lblUsuario.setForeground(Color.black); 
          
                    TelaPrincipal.menCadMoto.setEnabled(true);
                    TelaPrincipal.menCadMarca.setEnabled(true);
                    TelaPrincipal.menCadServico.setEnabled(true);
                    TelaPrincipal.menCadEstoque.setEnabled(true);
                    TelaPrincipal.menCadProduto.setEnabled(true);
                    

                    this.dispose();

                }
            } else {
                
                JOptionPane.showMessageDialog(null, "usuario e/ou senha invalido(s)"); 
            }

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, e);

        }
    }

    public TelaLogin() {
        initComponents();

        conexao = ModuloConexao.conector();
        senhaPadrao = txtSenha.getEchoChar();
        getRootPane().setDefaultButton(btnEntrar);
        

        //a linha abaixo serve de apoio na visualização do status da conexão
        if (conexao != null) {
            //exemplo de parametro de texto para interatividade na tela de login.
            // lblstatus.setText('Conectado') --> aparecerá online, caso dê tudo certo no try-catch na conexão sql 
            // lblstatus.setText('Offline') --> apareça offline caso tenha algum erro na String da conexao sql.
            //definindo o status do png de acordo com a preferência nesse caso, quero que apareça online. //caminho do arquivo
            
            
             try {
                 
                 chkSenha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/close_eye.png")));

                 
                 chkSenha.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/open_eye.png")));
    
            } catch (Exception e) {
                
                 System.out.println("Erro na imagem!!" + e);
            }

            
            try {
                
                lblStatusBD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/dtboffline.png")));
                
            } catch (Exception e) {
             
                lblStatusBD.setText("Offline");
                System.out.println("Erro imagem OFF: " + e);
                
            }
        
            
            
            try {
                
                lblStatusBD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/dtbonline.png")));
                
            } catch (Exception e) {
                
                lblStatusBD.setText("Conectado");
                System.out.println("Erro imagem ON: " + e);
            }

        } else {
            
            try {
                
                lblStatusBD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/dtboffline.png")));
            } catch (Exception e) {
                
                lblStatusBD.setText("Offline");
                System.out.println("Erro imagem OFF: " + e);
            }
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        btnEntrar = new javax.swing.JButton();
        txtSenha = new javax.swing.JPasswordField();
        lblStatusBD = new javax.swing.JLabel();
        chkSenha = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MOTO GESTOR - LOGIN");
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel1.setText("Usuário");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 13)); // NOI18N
        jLabel2.setText("Senha");

        btnEntrar.setText("Entrar");
        btnEntrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEntrarActionPerformed(evt);
            }
        });

        lblStatusBD.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/dtboffline.png"))); // NOI18N
        lblStatusBD.setPreferredSize(new java.awt.Dimension(50, 50));

        chkSenha.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        chkSenha.setContentAreaFilled(false);
        chkSenha.setFocusPainted(false);
        chkSenha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/close_eye.png"))); // NOI18N
        chkSenha.setMaximumSize(new java.awt.Dimension(30, 30));
        chkSenha.setPreferredSize(new java.awt.Dimension(30, 30));
        chkSenha.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkSenhaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnEntrar)
                .addGap(92, 92, 92)
                .addComponent(lblStatusBD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(110, 110, 110)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(chkSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(175, 175, 175)
                        .addComponent(jLabel2)))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(chkSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStatusBD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(btnEntrar)))
                .addContainerGap(28, Short.MAX_VALUE))
        );

        setSize(new java.awt.Dimension(416, 296));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnEntrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEntrarActionPerformed

       logar();
    }//GEN-LAST:event_btnEntrarActionPerformed

    private void chkSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSenhaActionPerformed
        if (chkSenha.isSelected()) {
            
            txtSenha.setEchoChar((char) 0);
        } // Se desmarcou -> Esconde (volta a bolinha padrão)
        
        else {
            txtSenha.setEchoChar(senhaPadrao);
        }
    }//GEN-LAST:event_chkSenhaActionPerformed

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
            java.util.logging.Logger.getLogger(TelaLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaLogin.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TelaLogin().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEntrar;
    private javax.swing.JCheckBox chkSenha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel lblStatusBD;
    private javax.swing.JPasswordField txtSenha;
    private javax.swing.JTextField txtUsuario;
    // End of variables declaration//GEN-END:variables
}

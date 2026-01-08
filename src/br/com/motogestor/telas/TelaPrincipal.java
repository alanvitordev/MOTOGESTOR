package br.com.motogestor.telas;

import br.com.motogestor.DAL.*;
import br.com.motogestor.telas.*;
import br.com.motogestor.DAL.ModuloConexao;
import java.text.DateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.HashMap;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class TelaPrincipal extends javax.swing.JFrame {

    Connection conexao = null;
    public static int idUser;
    private static String usuarioLogado;

    public TelaPrincipal(int idUser, String usuarioLogado) {
        initComponents();

        
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        this.usuarioLogado = usuarioLogado;
        lblUsuario.setText(this.usuarioLogado);
    }
    
     public int getIdUser() {
        return idUser;
    }

    public String getUsuarioLogado() {
        return usuarioLogado;
    }
    
     public TelaPrincipal() {
         
        initComponents();
        conexao = ModuloConexao.conector();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        desktop = new javax.swing.JDesktopPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lblUsuario = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        Menu = new javax.swing.JMenuBar();
        menCad = new javax.swing.JMenu();
        menCadCli = new javax.swing.JMenuItem();
        menCadOs = new javax.swing.JMenuItem();
        menCadUso = new javax.swing.JMenuItem();
        menCadMoto = new javax.swing.JMenuItem();
        menCadFornecedor = new javax.swing.JMenuItem();
        menCadProduto = new javax.swing.JMenuItem();
        menCadServico = new javax.swing.JMenuItem();
        menCadMecanico = new javax.swing.JMenuItem();
        menCadMarca = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        menCadEstoque = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        menRel = new javax.swing.JMenu();
        MenRelCli = new javax.swing.JMenuItem();
        menRelSer = new javax.swing.JMenuItem();
        menAju = new javax.swing.JMenu();
        menAjuSob = new javax.swing.JMenuItem();
        menOpc = new javax.swing.JMenu();
        menOpcSai = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MOTO GESTOR - CONTROLE DE OS");
        setAutoRequestFocus(false);
        setBackground(new java.awt.Color(255, 255, 255));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        desktop.setPreferredSize(new java.awt.Dimension(850, 600));
        desktop.setRequestFocusEnabled(false);

        javax.swing.GroupLayout desktopLayout = new javax.swing.GroupLayout(desktop);
        desktop.setLayout(desktopLayout);
        desktopLayout.setHorizontalGroup(
            desktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 850, Short.MAX_VALUE)
        );
        desktopLayout.setVerticalGroup(
            desktopLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );

        getContentPane().add(desktop, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 850, 600));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/novaLogo (1).png"))); // NOI18N
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 180, 380, 410));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Usuário:");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 30, -1, -1));

        lblUsuario.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblUsuario.setText("usuario");
        getContentPane().add(lblUsuario, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 60, -1, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel3.setText("Data:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 100, -1, -1));

        lblData.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblData.setText("data");
        getContentPane().add(lblData, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 130, -1, -1));

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 590, 380, 10));

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 160, 380, 10));

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 376, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 6, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(850, 0, 380, -1));

        menCad.setText("Cadastro");

        menCadCli.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK));
        menCadCli.setText("Cliente");
        menCadCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadCliActionPerformed(evt);
            }
        });
        menCad.add(menCadCli);

        menCadOs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        menCadOs.setText("Ordem de Serviço (OS)");
        menCadOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadOsActionPerformed(evt);
            }
        });
        menCad.add(menCadOs);

        menCadUso.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.ALT_MASK));
        menCadUso.setText("Usuário");
        menCadUso.setEnabled(false);
        menCadUso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadUsoActionPerformed(evt);
            }
        });
        menCad.add(menCadUso);

        menCadMoto.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.ALT_MASK));
        menCadMoto.setText("Moto");
        menCadMoto.setEnabled(false);
        menCadMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadMotoActionPerformed(evt);
            }
        });
        menCad.add(menCadMoto);

        menCadFornecedor.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.ALT_MASK));
        menCadFornecedor.setText("Fornecedor");
        menCadFornecedor.setEnabled(false);
        menCadFornecedor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadFornecedorActionPerformed(evt);
            }
        });
        menCad.add(menCadFornecedor);

        menCadProduto.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        menCadProduto.setText("Produto");
        menCadProduto.setEnabled(false);
        menCadProduto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadProdutoActionPerformed(evt);
            }
        });
        menCad.add(menCadProduto);

        menCadServico.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        menCadServico.setText("Serviços");
        menCadServico.setEnabled(false);
        menCadServico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadServicoActionPerformed(evt);
            }
        });
        menCad.add(menCadServico);

        menCadMecanico.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        menCadMecanico.setText("Mecânico");
        menCadMecanico.setEnabled(false);
        menCadMecanico.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadMecanicoActionPerformed(evt);
            }
        });
        menCad.add(menCadMecanico);

        menCadMarca.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.SHIFT_MASK));
        menCadMarca.setText("Marcas");
        menCadMarca.setEnabled(false);
        menCadMarca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadMarcaActionPerformed(evt);
            }
        });
        menCad.add(menCadMarca);

        Menu.add(menCad);

        jMenu1.setText("Movimentações");

        menCadEstoque.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.ALT_MASK));
        menCadEstoque.setText("Estoque");
        menCadEstoque.setEnabled(false);
        menCadEstoque.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menCadEstoqueActionPerformed(evt);
            }
        });
        jMenu1.add(menCadEstoque);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Precificação");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        Menu.add(jMenu1);

        jMenu2.setText("Consultas");

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Lista de Produtos");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);

        Menu.add(jMenu2);

        menRel.setText("Relatório");
        menRel.setEnabled(false);

        MenRelCli.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        MenRelCli.setText("Clientes");
        MenRelCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenRelCliActionPerformed(evt);
            }
        });
        menRel.add(MenRelCli);

        menRelSer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        menRelSer.setText("Serviços");
        menRelSer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menRelSerActionPerformed(evt);
            }
        });
        menRel.add(menRelSer);

        Menu.add(menRel);

        menAju.setText("Ajuda");

        menAjuSob.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, java.awt.event.InputEvent.ALT_MASK));
        menAjuSob.setText("Sobre");
        menAjuSob.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menAjuSobActionPerformed(evt);
            }
        });
        menAju.add(menAjuSob);

        Menu.add(menAju);

        menOpc.setText("Opções");

        menOpcSai.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        menOpcSai.setText("Sair");
        menOpcSai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menOpcSaiActionPerformed(evt);
            }
        });
        menOpc.add(menOpcSai);

        Menu.add(menOpc);

        setJMenuBar(Menu);

        setSize(new java.awt.Dimension(1248, 660));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void menCadOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadOsActionPerformed
        //AS LINHAS ABAIXO IRÃO ABRIR O FORM "TelaOS"

        TelaOS os = new TelaOS(this.getIdUser());
        os.setVisible(true);

        desktop.add(os);
    }//GEN-LAST:event_menCadOsActionPerformed

    private void menCadUsoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadUsoActionPerformed
        // as linhas abaixo vão abrir o form "TelaUsuário" dentro do desktop "quadro preto da tela principal".

        TelaUsuario usuario = new TelaUsuario();
        usuario.setVisible(true);

        desktop.add(usuario);
    }//GEN-LAST:event_menCadUsoActionPerformed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        // as linhas abaixo substituem a label data 'lbldata' pela data atual do sistema ao inicializar o form.
        Date data = new Date();
        DateFormat formatador = DateFormat.getDateInstance(DateFormat.SHORT); //DateFormat faz as conversões automaticamenter sem precisar transformar em String.
        lblData.setText(formatador.format(data)); //substituição pela lblData pela data atual do sistema

    }//GEN-LAST:event_formWindowActivated

    private void menOpcSaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menOpcSaiActionPerformed
        // exibe uma caixa de diálogo
        int sair = JOptionPane.showConfirmDialog(null, "Tem certeza que quer sair do sistema?", "Atenção", JOptionPane.YES_NO_OPTION);
        //texto da caixa do diálogo          //titulo da caixa   //opção sim ou não

        if (sair == JOptionPane.YES_NO_OPTION) {

            System.exit(0);

        }

    }//GEN-LAST:event_menOpcSaiActionPerformed

    private void menAjuSobActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menAjuSobActionPerformed
        // chamando a tela Sobre

        TelaSobre sobre = new TelaSobre();
        sobre.setVisible(true);
    }//GEN-LAST:event_menAjuSobActionPerformed

    private void menCadCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadCliActionPerformed
        // chamando a tela Cliente --> Menu/Cadastro/Cliente

        TelaCliente cliente = new TelaCliente(this.getIdUser());
        cliente.setVisible(true);
        desktop.add(cliente);
    }//GEN-LAST:event_menCadCliActionPerformed

    private void menRelSerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menRelSerActionPerformed
        // GERANDO UM RELATÓRIO DE SERVIÇOS

      // GERANDO UM RELATÓRIO DE SERVIÇOS

        int confirmacao = JOptionPane.showConfirmDialog(null, "Confirma a emissão desse relatório? ", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) { // Ajustei levemente para YES_OPTION (que é o correto para o 'Sim')

            //emitindo relatório com o framework JasperReports
            try {
                // --- INÍCIO DO QUE FOI ADICIONADO (NECESSÁRIO) ---
                String num_os = JOptionPane.showInputDialog(null, "Digite o número da OS: ");
                
                HashMap filtro = new HashMap();
                filtro.put("Os_2", Integer.parseInt(num_os));
                // --- FIM DO QUE FOI ADICIONADO ---

                //usando a classe JasperPrint para preparar a impressão de um relatório.
                // Mudei aqui: troquei 'null' por 'filtro' para enviar o número digitado
                JasperPrint emissao = JasperFillManager.fillReport("C:\\PLAYLIST JOSE DE ASSIS JAVASQL\\IREPORT (RELATÓRIOS)\\reports (relatórios criados pelo ireport)\\Servicos.jasper", filtro, conexao);

                // a linha abaixo exibe o relatório
                JasperViewer.viewReport(emissao, false);

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }


    }//GEN-LAST:event_menRelSerActionPerformed

    private void MenRelCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenRelCliActionPerformed
        // GERANDO UM RELATÓRIO DE CLIENTES

        int confirmacao = JOptionPane.showConfirmDialog(null, "Confirma a impressão desse relatório? ", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_NO_OPTION) {

            //imprimindo relatório com o framework JasperReports
            try {
                //usando a classe JasperPrint para preparar a impressão de um relatório.

                JasperPrint impressao = JasperFillManager.fillReport("C:\\PLAYLIST JOSE DE ASSIS JAVASQL\\IREPORT (RELATÓRIOS)\\reports (relatórios criados pelo ireport)\\Clientes.jasper", null, conexao);

                // a linha abaixo exibe o relatório
                JasperViewer.viewReport(impressao, false);

            } catch (Exception e) {

                JOptionPane.showConfirmDialog(null, "Ação cancelada pelo usuário.");
            }

        }


    }//GEN-LAST:event_MenRelCliActionPerformed

    private void menCadMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadMotoActionPerformed
        // chamando a tela Moto --> Menu/Cadastro/Moto

        TelaMoto moto = new TelaMoto(this.getIdUser());
        desktop.add(moto);
        moto.pack();
        moto.setVisible(true);


    }//GEN-LAST:event_menCadMotoActionPerformed

    private void menCadFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadFornecedorActionPerformed
        // chamando a tela Fornecedor --> Menu/Cadastro/Fornecedor

        TelaFornecedor fornecedor = new TelaFornecedor(this.getIdUser());
        fornecedor.setVisible(true);
        desktop.add(fornecedor);
    }//GEN-LAST:event_menCadFornecedorActionPerformed

    private void menCadProdutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadProdutoActionPerformed
        TelaProduto produto = new TelaProduto(this.getIdUser());
        produto.setVisible(true);
        desktop.add(produto);
    }//GEN-LAST:event_menCadProdutoActionPerformed

    private void menCadEstoqueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadEstoqueActionPerformed
        TelaEstoque estoque = new TelaEstoque(this.getIdUser());
        estoque.setVisible(true);
        desktop.add(estoque);
    }//GEN-LAST:event_menCadEstoqueActionPerformed

    private void menCadServicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadServicoActionPerformed
        // chamando a tela Fornecedor --> Menu/Cadastro/Fornecedor

        TelaServico servico = new TelaServico(this.getIdUser());
        servico.setVisible(true);
        desktop.add(servico);
    }//GEN-LAST:event_menCadServicoActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // Abrir a tela de precificação

        TelaPrecificacao preco = new TelaPrecificacao(this.getIdUser());
        preco.setVisible(true);
        desktop.add(preco);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        

        TelaLocalizarProduto localizar = new TelaLocalizarProduto();
        localizar.setVisible(true);
        desktop.add(localizar);


    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void menCadMecanicoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadMecanicoActionPerformed
        TelaMecanico mecanico = new TelaMecanico(this.getIdUser());
        mecanico.setVisible(true);
        desktop.add(mecanico);

    }//GEN-LAST:event_menCadMecanicoActionPerformed

    private void menCadMarcaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menCadMarcaActionPerformed
         TelaMarcas marca = new TelaMarcas(this.getIdUser());
        marca.setVisible(true);
        desktop.add(marca);
    }//GEN-LAST:event_menCadMarcaActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_formComponentResized

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
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TelaPrincipal.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
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
                new TelaPrincipal().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem MenRelCli;
    private javax.swing.JMenuBar Menu;
    public static javax.swing.JDesktopPane desktop;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    public static javax.swing.JLabel lblData;
    public static javax.swing.JLabel lblUsuario;
    private javax.swing.JMenu menAju;
    private javax.swing.JMenuItem menAjuSob;
    private javax.swing.JMenu menCad;
    public static javax.swing.JMenuItem menCadCli;
    public static javax.swing.JMenuItem menCadEstoque;
    public static javax.swing.JMenuItem menCadFornecedor;
    public static javax.swing.JMenuItem menCadMarca;
    public static javax.swing.JMenuItem menCadMecanico;
    public static javax.swing.JMenuItem menCadMoto;
    public static javax.swing.JMenuItem menCadOs;
    public static javax.swing.JMenuItem menCadProduto;
    public static javax.swing.JMenuItem menCadServico;
    public static javax.swing.JMenuItem menCadUso;
    private javax.swing.JMenu menOpc;
    private javax.swing.JMenuItem menOpcSai;
    public static javax.swing.JMenu menRel;
    public static javax.swing.JMenuItem menRelSer;
    // End of variables declaration//GEN-END:variables
}

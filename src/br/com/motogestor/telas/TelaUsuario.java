
package br.com.motogestor.telas;

//AULA 13 - JOSE DE ASSIS (CRUD)

//Reutiliza-se algumas coisas da JForm (Painel) da TelaLogin.

import java.sql.*;

import br.com.motogestor.DAL.ModuloConexao;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;
import br.com.motogestor.util.CriptografiaUtil;

public class TelaUsuario extends javax.swing.JInternalFrame {
    
    Connection conexao = null; 
    PreparedStatement pst = null; 
    ResultSet rs = null; 
    char senhaPadrao;

    public TelaUsuario() {  //CLASSE CONSTRUTORA
        initComponents();

        //CONEXAO SQL
        conexao = ModuloConexao.conector();
        senhaPadrao = txtUsuSenha.getEchoChar();
        pesquisarUsuario();

        try {

            chkSenha.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/close_eye.png")));

            chkSenha.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/open_eye.png")));

        } catch (Exception e) {

            System.out.println("Erro as imagens!: " + e);
        }

    }

    //MÉTODO PARA ADICIONAR USUÁRIOS "C" --> CRUD
    private void adicionarUsuario() {

        String sql_valida = "SELECT 1 FROM tbusuarios WHERE login = ?";

        String sql_adicionar = "insert into tbusuarios (usuario, telefone, login, senha, perfil, status) values (?, ?, ?, ?, ?, 'Ativo')";

        try {

            String nome = txtUsuNome.getText().toString().trim();
            String fone = txtUsuFone.getText().toString().trim();
            String login = txtUsuLogin.getText().toString().trim();
            String senha = new String(txtUsuSenha.getPassword()).trim();

            // Pegando o ComboBox de forma segura (evitando NullPointerException)
            Object perfilObj = cboUsuPerfil.getSelectedItem();
            String perfilStr = (perfilObj == null) ? "" : perfilObj.toString().trim();

            if (nome.isEmpty() || login.isEmpty() || senha.isEmpty() || perfilStr.isEmpty() || perfilStr.equals(" ")) {

                //lê-se, se o campo txtUsuId estiver vazio. 
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return; // Sai do método se a validação falhar
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, login); // Checa o Login
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {

                JOptionPane.showMessageDialog(null, "Atenção: Este login já está em uso!");
                return; // Para o método
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, nome);
            pst.setString(2, txtUsuFone.getText().replaceAll("[^0-9]", ""));
            pst.setString(3, login);

            String senhaCriptografada = CriptografiaUtil.criptografar(senha);
            pst.setString(4, senhaCriptografada);

            //o combo box está sendo convertido para string... (seu comentário original)
            //com o .toString(); (seu comentário original)
            pst.setString(5, perfilStr);

            //a estrutura abaixo é usada para confirmar a inserção de dados na tabela do sql.
            int adicionado = pst.executeUpdate(); //atribuindo a execucação a variável "adicionado".

            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Usuário adicionado com sucesso!");

                //os códigos abaixo limpam os campos após o cadastro de um usuário.
                limparCampos();
                pesquisarUsuario();

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

            limparCampos();

        }

    }

    private void pesquisarUsuario() {

        String sql = "SELECT idusuario as ID, usuario as CARGO, telefone as TELEFONE, login as LOGIN, "
                + "perfil as PERFIL, "
                + "CASE WHEN status = TRUE THEN 'Ativo' ELSE 'Inativo' END AS STATUS "
                + "FROM tbusuarios WHERE usuario LIKE ?";

        try {

            pst = conexao.prepareStatement(sql);

            //passando o conteúdo da caixa de pesquisa para o "?".
            pst.setString(1, txtPesqUser.getText() + "%");
            rs = pst.executeQuery();

            tblUsuarios.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void setarCampos() {

        int setar = tblUsuarios.getSelectedRow();

        if (setar == -1) {
            return;
        }

        String id = tblUsuarios.getModel().getValueAt(setar, 0).toString();
        String cargo = tblUsuarios.getModel().getValueAt(setar, 1).toString();
        Object telObj = tblUsuarios.getModel().getValueAt(setar, 2);
        String login = tblUsuarios.getModel().getValueAt(setar, 3).toString();

        txtUsuSenha.setText(null);

        String perfil = tblUsuarios.getModel().getValueAt(setar, 4).toString();

        // caso a coluna telefone venha a ser vazia quando eu clicar na jtable.
        String telefone = (telObj == null) ? "" : telObj.toString();

        txtUsuId.setText(id);
        txtUsuNome.setText(cargo);
        txtUsuFone.setText(telefone);

        txtUsuLogin.setText(login);
        //txtUsuSenha.setText(senha);
        cboUsuPerfil.setSelectedItem(perfil);

        logicaTbl();

    }

    // CRIANDO O MÉTODO alterar() = "U" --> CRUD.
    private void alterarUsuario() {

        String sql_valida = "SELECT 1 FROM tbusuarios WHERE login = ? AND idusuario <> ?";

        // REMOVIDO: String sql_alterar... (A query agora é definida dinamicamente abaixo)
        try {

            String nome = txtUsuNome.getText().toString().trim();

            //Adicionado .replaceAll para limpar a máscara do telefone
            String fone = txtUsuFone.getText().replaceAll("[^0-9]", "");
            String login = txtUsuLogin.getText().toString().trim();
            String senha = new String(txtUsuSenha.getPassword()).trim();
            String idUserStr = txtUsuId.getText().trim();

            Object perfilObj = cboUsuPerfil.getSelectedItem();
            String perfilStr = (perfilObj == null) ? "" : perfilObj.toString().trim();

            if (idUserStr.isEmpty() || nome.isEmpty() || login.isEmpty() || perfilStr.isEmpty() || perfilStr.equals(" ")) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return;
            }

            int idUser;

            try {

                idUser = Integer.parseInt(idUserStr);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do usuário é inválido.");
                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, login); // Checa o Login
            pst_valida.setInt(2, idUser);   // Ignora o ID atual
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                JOptionPane.showMessageDialog(null, "Atenção: Este login já está em uso por outro usuário!");
                return; // Para o método
            }

            if (senha.isEmpty()) {
                //Senha vazia -> Mantém a antiga
                String sql_sem_senha = "update tbusuarios set usuario =?, telefone =?, login =?, perfil =?, data = now() where idusuario =?";

                pst = conexao.prepareStatement(sql_sem_senha);

                pst.setString(1, nome);
                pst.setString(2, fone);
                pst.setString(3, login);
                pst.setString(4, perfilStr);
                pst.setInt(5, idUser);

            } else {

                String sql_busca_senha = "SELECT senha FROM tbusuarios WHERE idusuario = ?";
                PreparedStatement pst_check = conexao.prepareStatement(sql_busca_senha);
                pst_check.setInt(1, idUser);
                ResultSet rs_check = pst_check.executeQuery();

                if (rs_check.next()) {

                    String senhaAtualBanco = rs_check.getString("senha"); // Pega o hash do banco
                    String novaSenhaHash = CriptografiaUtil.criptografar(senha); // Cria o hash da nova

                    if (novaSenhaHash.equals(senhaAtualBanco)) {

                        JOptionPane.showMessageDialog(null, "A nova senha não pode ser igual à senha atual!");
                        return; // Para tudo e não deixa salvar
                    }

                    // Precisamos setar a senha criptografada para usar no insert abaixo
                    senha = novaSenhaHash; //atualizo a variável senha com o hash para usar ali embaixo
                }

                String sql_com_senha = "update tbusuarios set usuario =?, telefone =?, login =?, senha =?, perfil =?, data = now() where idusuario =?";

                pst = conexao.prepareStatement(sql_com_senha);

                pst.setString(1, nome);
                pst.setString(2, fone);
                pst.setString(3, login);

                pst.setString(4, senha);

                pst.setString(5, perfilStr);
                pst.setInt(6, idUser);
            }

            int adicionado = pst.executeUpdate();

            if (adicionado > 0) {

                JOptionPane.showMessageDialog(null, "Dados alterados com sucesso!");

                limparCampos();
                logicaUpdate();
                pesquisarUsuario();

            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void inativarUsuario() {

        int inativacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja INATIVAR este usuário?",
                "Atenção", JOptionPane.YES_NO_CANCEL_OPTION);

        if (inativacao == JOptionPane.YES_OPTION) {

            String sql = "UPDATE tbusuarios SET status = 'Inativo' WHERE idusuario = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtUsuId.getText());

                int usuarioInativado = pst.executeUpdate();

                if (usuarioInativado > 0) {

                    JOptionPane.showMessageDialog(null, "Usuário inativado com sucesso!");

                    limparCampos();
                    logicaInativar();
                    pesquisarUsuario();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarUsuario() {

        int inativacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja ATIVAR este usuário?",
                "Atenção", JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbusuarios SET status = 'Ativo' WHERE idusuario = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setString(1, txtUsuId.getText());

                int usuarioAtivado = pst.executeUpdate();

                if (usuarioAtivado >= 1) {

                    JOptionPane.showMessageDialog(null, "Usuário ativado com sucesso!");

                    limparCampos();
                    logicaAtivar();
                    pesquisarUsuario();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void chkMostrarSenha(java.awt.event.ItemEvent evt) {

        if (chkSenha.isSelected()) {
            // Mostra a senha (remove a máscara)
            txtUsuSenha.setEchoChar((char) 0);

        } else {

            txtUsuSenha.setEchoChar(senhaPadrao);
        }
    }

    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblUsuarios.setEnabled(true);
        txtPesqUser.setEnabled(true);

    }

    private void logicaTbl() {

        btnAdicionar.setEnabled(false);

        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);

    }

    private void logicaAtivar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblUsuarios.setEnabled(true);
        txtPesqUser.setEnabled(true);

    }

    private void logicaInativar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblUsuarios.setEnabled(true);
        txtPesqUser.setEnabled(true);

    }

    private void limpezaGeralTbl() {

        txtUsuId.setText(null);
        cboUsuPerfil.setSelectedItem(" ");
        txtUsuNome.setText(null);
        txtUsuFone.setText(null);
        cboUsuPerfil.setSelectedItem(" ");
        txtUsuLogin.setText(null);
        txtUsuSenha.setText(null);

        btnAdicionar.setEnabled(true);
        tblUsuarios.setEnabled(true);
        txtPesqUser.setEnabled(true);

        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void limparCampos() {

        // limpa os campos
        txtUsuId.setText(null);
        cboUsuPerfil.setSelectedItem(" ");
        txtUsuNome.setText(null);
        txtUsuFone.setText(null);
        cboUsuPerfil.setSelectedItem(" ");
        txtUsuLogin.setText(null);
        txtUsuSenha.setText(null);

    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        pnlUsuarios = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        txtUsuId = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txtPesqUser = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtUsuNome = new javax.swing.JTextField();
        btnLimparDados = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtUsuLogin = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        cboUsuPerfil = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        btnAtivar = new javax.swing.JButton();
        btnInativar = new javax.swing.JButton();
        txtUsuSenha = new javax.swing.JPasswordField();
        chkSenha = new javax.swing.JCheckBox();
        txtUsuFone = new javax.swing.JFormattedTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR USUÁRIO");
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setPreferredSize(new java.awt.Dimension(850, 600));
        try {
            setSelected(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setViewportBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        pnlUsuarios.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlUsuariosMouseClicked(evt);
            }
        });

        tblUsuarios = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblUsuarios.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "CARGO", "TELEFONE", "LOGIN", "PERFIL", "STATUS"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblUsuarios.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblUsuariosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblUsuarios);

        txtUsuId.setEditable(false);

        jLabel1.setText("* ID:");

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N

        txtPesqUser.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqUserKeyReleased(evt);
            }
        });

        jLabel8.setText("USUÁRIO:");

        javax.swing.GroupLayout pnlUsuariosLayout = new javax.swing.GroupLayout(pnlUsuarios);
        pnlUsuarios.setLayout(pnlUsuariosLayout);
        pnlUsuariosLayout.setHorizontalGroup(
            pnlUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlUsuariosLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqUser, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtUsuId, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(jScrollPane1)
        );
        pnlUsuariosLayout.setVerticalGroup(
            pnlUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlUsuariosLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(pnlUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addGroup(pnlUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtPesqUser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel8))
                    .addGroup(pnlUsuariosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtUsuId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        txtUsuNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsuNomeActionPerformed(evt);
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

        jLabel2.setText("* CARGO:");

        jLabel3.setText("* LOGIN:");

        txtUsuLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsuLoginActionPerformed(evt);
            }
        });

        jLabel4.setText("* SENHA:");

        jLabel6.setText("TELEFONE:");

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarPessoas.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Usuário");
        btnAdicionar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarPessoas.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Dados");
        btnAlterar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        cboUsuPerfil.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "ADMIN", "USER" }));
        cboUsuPerfil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboUsuPerfilActionPerformed(evt);
            }
        });

        jLabel5.setText("PERFIL:");

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Usuário");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Usuário");
        btnInativar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 48));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

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

        try {
            txtUsuFone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##) #####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel2))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtUsuNome)
                                    .addComponent(txtUsuLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(5, 5, 5)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtUsuSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addComponent(chkSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtUsuFone, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(86, 86, 86)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboUsuPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(57, 57, 57)
                .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(47, 47, 47))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19)))
                .addGap(0, 20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(pnlUsuarios, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsuNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtUsuLogin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(26, 26, 26)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtUsuSenha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkSenha, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel5)
                    .addComponent(cboUsuPerfil, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsuFone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jScrollPane2.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 824, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE)
                .addContainerGap())
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void tblUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblUsuariosMouseClicked
        setarCampos();
    }//GEN-LAST:event_tblUsuariosMouseClicked

    private void txtPesqUserKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqUserKeyReleased
        pesquisarUsuario();
    }//GEN-LAST:event_txtPesqUserKeyReleased

    private void pnlUsuariosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlUsuariosMouseClicked

        tblUsuarios.clearSelection();

        limpezaGeralTbl();
    }//GEN-LAST:event_pnlUsuariosMouseClicked

    private void txtUsuNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsuNomeActionPerformed
        //chamando o método consultar (campo nome/cadastro usuário
    }//GEN-LAST:event_txtUsuNomeActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void txtUsuLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsuLoginActionPerformed
        //chamando o método consultar (campo login/cadastro usuário
    }//GEN-LAST:event_txtUsuLoginActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        //chamando o metódo adicionar --> "C" = CRUD

        adicionarUsuario();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        // chamando o´método alterar --> "U" = CRUD

        alterarUsuario();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void cboUsuPerfilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboUsuPerfilActionPerformed

    }//GEN-LAST:event_cboUsuPerfilActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarUsuario();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        // chamando o método InativarUsuario();

        inativarUsuario();
    }//GEN-LAST:event_btnInativarActionPerformed

    private void chkSenhaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSenhaActionPerformed
       if (chkSenha.isSelected()) {
        // Mostra a senha (remove a máscara)
        txtUsuSenha.setEchoChar((char)0); 
        
    } else {
           
        // Restaura a bolinha original
        txtUsuSenha.setEchoChar(senhaPadrao); 
    }
    }//GEN-LAST:event_chkSenhaActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JComboBox<String> cboUsuPerfil;
    private javax.swing.JCheckBox chkSenha;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel pnlUsuarios;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtPesqUser;
    private javax.swing.JFormattedTextField txtUsuFone;
    private javax.swing.JTextField txtUsuId;
    private javax.swing.JTextField txtUsuLogin;
    private javax.swing.JTextField txtUsuNome;
    private javax.swing.JPasswordField txtUsuSenha;
    // End of variables declaration//GEN-END:variables
}

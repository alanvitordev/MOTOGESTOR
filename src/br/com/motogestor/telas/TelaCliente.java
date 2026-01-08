
package br.com.motogestor.telas;


import java.sql.*;
import br.com.motogestor.DAL.ModuloConexao;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
//a linha abaixo importa recursos da biblioteca rs2xml.jar...
import net.proteanit.sql.DbUtils; //biblioteca que será utilizada como recurso para preencher a tabela com os dados do cliente.

public class TelaCliente extends javax.swing.JInternalFrame {
    
    private int idUser;

  
    //PARAMETROS DA QUERY
    
    Connection conexao = null; //chamando a variável conexao criada em ModuloConexao, 'Connection é um framework do pacote importado.
    PreparedStatement pst = null; //PreparedStatement também é um framwework de manipução dos dados em sql, responsável pela consulta.
    ResultSet rs = null; //ResultSet servirá para exibir o resultado das instruções executadas no Java.

    public TelaCliente(int idUser) {
        
        initComponents();

        //CONEXAO SQL
        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        pesquisarCliente();
    }

    private void adicionarCliente() {

        String sql_valida = "SELECT 1 FROM tbclientes WHERE cliente = ?";

        String sql_adicionar = "insert into tbclientes (cliente, endereco, telefone, status, idusuario) values (?, ?, ?, 'Ativo', ?)"; 

        try {

            String nomeCli = txtCliNome.getText().toString().trim();
            String enderecoCli = txtCliEndereco.getText().toString().trim();
            String foneCli = txtCliFone.getText().toString().trim();

            
            if (nomeCli.isEmpty() || foneCli.isEmpty() || enderecoCli.isEmpty()) { 

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, nomeCli); // Valida o nome do cliente
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Um cliente com este nome já está cadastrado!");
                return; // Para o método aqui
            }
            
         
            //ao inves de pegar os dados já existentes no banco...
            //aqui cria-se o parametro para obter as informações digitadas...
            //em cada campo digitado abaixo, e assim os dados são coletados...
            //e enviados para o banco de dados.
            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, nomeCli); // Usando a variável
            pst.setString(2, enderecoCli); // Usando a variável
            pst.setString(3, txtCliFone.getText().replaceAll("[^0-9]", "")); 
            
            pst.setInt(4, this.idUser);

            //a estrutura abaixo é usada para confirmar a inserção de dados na tabela do sql. (seu comentário original)
            int incluido = pst.executeUpdate(); //atribuindo a execucação a variável "adicionado".

            //a linha abaixo serve como apoio para a compreensão da lógica (seu comentário original)
            //System.out.println(adicionado); //mostra no terminal o número de linhas adicionadas ao banco (cliente cadastrado).
            if (incluido > 0) {

                JOptionPane.showMessageDialog(null, "Cliente adicionado com sucesso!");
                
                limpezaDados();
 
                pesquisarCliente();
                
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        } 
    }

    //método para pesquisar clientes com filtro, ex: quando digitado a letra "J", o campo ira automaticamente buscar...
    
    //os clientes que tem a mesma inicial(ais).
    private void pesquisarCliente() {

        //Alteração feita para as tabelas aparecerem com o apelido definido no sql para cada coluna e também para exibir "Ativo e Inativo"...
        
        //na coluna status no Menu/Cliente.
        String sql = "SELECT idcli as ID, cliente as CLIENTE, endereco as ENDEREÇO, telefone as TELEFONE, "
                + " DATE_FORMAT(data,'%d/%m/%Y - %H:%i') AS DATA, "
                + "CASE WHEN status = TRUE THEN 'Ativo' ELSE 'Inativo' END AS STATUS "
                + "FROM tbclientes WHERE cliente LIKE ?";

        try {

            pst = conexao.prepareStatement(sql);

            //passando o conteúdo da caixa de pesquisa para o "?".
            pst.setString(1, txtCliPesquisar.getText() + "%"); //ATENÇÃO PARA NÃO ESQUECER O "%", ESSA LINHA CONTATENA O PERCENT.
            rs = pst.executeQuery();

            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela.
            tblClientes.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    //método para preencher os campos automaticamente quando clicar no nome do cliente para melhor manipular.
    private void preencherCamposCliente() {

        int preencher = tblClientes.getSelectedRow();

        //método para setar as tabelas do formulário.
        txtCliId.setText(tblClientes.getModel().getValueAt(preencher, 0).toString());
        txtCliNome.setText(tblClientes.getModel().getValueAt(preencher, 1).toString());
        txtCliEndereco.setText(tblClientes.getModel().getValueAt(preencher, 2).toString());
        txtCliFone.setText(tblClientes.getModel().getValueAt(preencher, 3).toString());
        
      logicaTbl();

    }

    private void atualizarCliente() {

        String sql_valida = "SELECT 1 FROM tbclientes WHERE cliente = ? AND idcli <> ?";

        String sql_alterar = "update tbclientes set cliente =?, endereco =?, telefone =?, idusuario=?, data = now() where idcli =?";

        try {
     
            String nomeCli = txtCliNome.getText().trim();
            String idCliStr = txtCliId.getText().trim(); 
            String foneCli = txtCliFone.getText().trim();
            String enderecoCli = txtCliEndereco.getText().trim();

            
            if (nomeCli.isEmpty() || foneCli.isEmpty() || enderecoCli.isEmpty() || idCliStr.isEmpty()) {
                
                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return; // Para o método
            }

            int idCli;
            
            try {
                
                idCli = Integer.parseInt(idCliStr);
                
            } catch (NumberFormatException e) {
                
                JOptionPane.showMessageDialog(null, "Erro: ID do cliente é inválido.");
                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, nomeCli);
            pst_valida.setInt(2, idCli);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                
                JOptionPane.showMessageDialog(null, "Atenção: Já existe outro cliente com este nome!");
                
                return; // Para o método
            }

            pst = conexao.prepareStatement(sql_alterar);
            pst.setString(1, nomeCli);
            pst.setString(2, enderecoCli);
            pst.setString(3, txtCliFone.getText().replaceAll("[^0-9]", ""));
            pst.setInt(4, this.idUser); 
            
            pst.setInt(5, idCli);

            int clienteAlter = pst.executeUpdate();

            if (clienteAlter > 0) {
                
                JOptionPane.showMessageDialog(null, "Dados alterados com sucesso!");
                
                limpezaDados();
                pesquisarCliente();
                
                logicaUpdate();
               
            } else {
                
                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {
            
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void inativarCliente() {

        int inativacao = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja INATIVAR este cliente?",
                "Atenção", JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbclientes SET status = 'Inativo', idusuario=? WHERE idcli = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtCliId.getText());

                int clienteInativado = pst.executeUpdate();

                if (clienteInativado > 0) {

                    JOptionPane.showMessageDialog(null, "Cliente inativado com sucesso!");

                    limpezaDados();
                    logicaInativar();
                    pesquisarCliente();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarCliente() {
   
        int confirma = JOptionPane.showConfirmDialog(null, "Tem certeza que deseja ATIVAR este cliente?", "Atenção!", JOptionPane.YES_NO_OPTION);

        if (confirma == JOptionPane.YES_OPTION) {
            
            String sql = "UPDATE tbclientes SET status = 'Ativo', idusuario =? WHERE idcli = ?";

            try {
                // Prepara e executa a instrução SQL
                pst = conexao.prepareStatement(sql);
                // Pega o ID do campo de texto do formulário
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtCliId.getText());
                

                // O executeUpdate() retorna o número de linhas que foram alteradas.
                // Se for maior que 0, significa que a alteração funcionou.
                int ativado = pst.executeUpdate();

                if (ativado >= 1) {
                    JOptionPane.showMessageDialog(null, "Cliente ativado com sucesso!");
                }

                limpezaDados();
                logicaAtivar();
                pesquisarCliente();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Erro ao ativar o cliente: " + e.getMessage());
            }
        }
    }

    private void limpezaDados() { //método criado para limpar os dados da tabela e manter os nomes criados no proprio editor JavaFx.
        
        txtCliPesquisar.setText(null);
        txtCliId.setText(null);
        txtCliNome.setText(null);
        txtCliEndereco.setText(null);
        txtCliFone.setText(null);

    }
    
     private void limpezaGeralTbl() { 
        
        txtCliPesquisar.setText(null);
        txtCliId.setText(null);
        txtCliNome.setText(null);
        txtCliEndereco.setText(null);
        txtCliFone.setText(null);
        
        btnAdicionar.setEnabled(true);
        
        tblClientes.setEnabled(true);
        txtCliPesquisar.setEnabled(true);
        
        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);


    }
    
    
    private void logicaUpdate(){
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblClientes.setEnabled(true);
        txtCliPesquisar.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
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
        tblClientes.setEnabled(true);
        txtCliPesquisar.setEnabled(true);
        
    }
    
    
    private void logicaInativar () {
        
        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
        tblClientes.setEnabled(true);
        txtCliPesquisar.setEnabled(true);
        
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        btnLimparDados = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        txtCliPesquisar = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtCliEndereco = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtCliId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtCliNome = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        btnAdicionar = new javax.swing.JButton();
        btnAlterar = new javax.swing.JButton();
        btnAtivar = new javax.swing.JButton();
        btnInativar = new javax.swing.JButton();
        txtCliFone = new javax.swing.JFormattedTextField();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - CADASTRAR CLIENTE");
        setToolTipText("");
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

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

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel1MouseClicked(evt);
            }
        });

        txtCliPesquisar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCliPesquisarActionPerformed(evt);
            }
        });
        txtCliPesquisar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtCliPesquisarKeyReleased(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel8.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel8.setPreferredSize(new java.awt.Dimension(25, 25));

        tblClientes = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;

            }
        };
        tblClientes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "CLIENTE", "ENDEREÇO", "TELEFONE", "STATUS", "DATA"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblClientes.setFocusable(false);
        tblClientes.getTableHeader().setReorderingAllowed(false);
        tblClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClientes);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("* CAMPOS OBRIGATÓRIOS");

        jLabel1.setText("CLIENTE:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtCliPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 180, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(25, 25, 25))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(29, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCliPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jLabel4.setText("* TELEFONE:");

        jLabel7.setText("ID:");

        txtCliId.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtCliId.setEnabled(false);
        txtCliId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCliIdActionPerformed(evt);
            }
        });

        jLabel2.setText("* NOME:");

        txtCliNome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCliNomeActionPerformed(evt);
            }
        });

        jLabel3.setText("* ENDEREÇO:");

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarPessoas.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Cliente");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 30));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
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

        try {
            txtCliFone.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.MaskFormatter("(##) #####-####")));
        } catch (java.text.ParseException ex) {
            ex.printStackTrace();
        }

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtCliEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, 477, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtCliFone, javax.swing.GroupLayout.PREFERRED_SIZE, 205, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(247, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, 507, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCliId, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(525, 525, 525)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtCliId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCliNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtCliEndereco, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtCliFone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(34, 34, 34)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );

        jScrollPane2.setViewportView(jPanel3);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 570, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limpezaDados();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void txtCliPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCliPesquisarActionPerformed
        //ADICIONEI O EVENTO ERRO PARA O CAMPO DE TEXTO "PESQUISA".
    }//GEN-LAST:event_txtCliPesquisarActionPerformed

    private void txtCliPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCliPesquisarKeyReleased
        //chamando o método PesquisarCliente();

        pesquisarCliente();
    }//GEN-LAST:event_txtCliPesquisarKeyReleased

    private void tblClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesMouseClicked
        // o código evento que será usado para setar os campos da tabela ao clicar com o mouse em algum campo.

        //chamando o método para puxar oque foi criado dentro do mesmo.

        preencherCamposCliente();
    }//GEN-LAST:event_tblClientesMouseClicked

    private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked

        tblClientes.clearSelection();
        limpezaGeralTbl();
    }//GEN-LAST:event_jPanel1MouseClicked

    private void txtCliIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCliIdActionPerformed
        //ADICIONEI UM EVENTO SEM QUERER AQUI A CAIXA DE TEXTO EM FRENTE AO "NOME:"
    }//GEN-LAST:event_txtCliIdActionPerformed

    private void txtCliNomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCliNomeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtCliNomeActionPerformed

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        //chamando o método criado dentro da classe AdicionarCliente();

        adicionarCliente();
    }//GEN-LAST:event_btnAdicionarActionPerformed

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        //chamando o método AtualizarCliente();

        atualizarCliente();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarCliente();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        //chamando o método InativarCliente();

        inativarCliente();
    }//GEN-LAST:event_btnInativarActionPerformed


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
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtCliEndereco;
    private javax.swing.JFormattedTextField txtCliFone;
    private javax.swing.JTextField txtCliId;
    private javax.swing.JTextField txtCliNome;
    private javax.swing.JTextField txtCliPesquisar;
    // End of variables declaration//GEN-END:variables
}

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

import java.sql.*;
import br.com.motogestor.DAL.ModuloConexao;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;

public class TelaMoto extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
     private int idUser;

    public TelaMoto(int idUser) {
        
        initComponents();

        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        
        pesquisarMoto();
        pesquisarCliente();
    }

    private void adicionarMoto() {

        String sql_valida = "SELECT 1 FROM tbmotos WHERE placa = ?";

        String sql_adicionar = "insert into tbmotos (modelo, placa, ano, cor, idcli, status, idusuario) values (?, ?, ?, ?, ?, 'Ativa', ?)";

        try {

            String modelo = txtModeloMoto.getText().toString().trim();
            String placa = txtPlacaMoto.getText().toString().trim();

            String ano = txtAnoMoto.getText().toString().trim();
            String cor = txtCorMoto.getText().toString().trim();
            String idCliStr = txtIdCliMoto.getText().toString().trim();

            if (idCliStr.isEmpty() || modelo.isEmpty() || placa.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return;
            }

            int idCli;

            try {

                idCli = Integer.parseInt(idCliStr);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do Cliente é inválido.");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, placa);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {

                JOptionPane.showMessageDialog(null, "Atenção: Uma moto com esta placa já está cadastrada!");
                return;
            }

            pst = conexao.prepareStatement(sql_adicionar);

            pst.setString(1, modelo);
            pst.setString(2, placa);

            pst.setString(3, ano);
            pst.setString(4, cor);
            pst.setInt(5, idCli);
            pst.setInt(6, TelaPrincipal.idUser);

            int inserido = pst.executeUpdate();

            if (inserido > 0) {

                JOptionPane.showMessageDialog(null, "Véiculo adicionado com sucesso!");

                limparCampos();
                pesquisarMoto();
                pesquisarCliente();

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void pesquisarCliente() {

        String sql = "select idcli as ID, cliente as NOME, telefone as TELEFONE from tbclientes where cliente like ?";

        try {

            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtPesqCli.getText() + "%");

            rs = pst.executeQuery();
            tblClientes.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void preencherTblCli() {

        int preencher = tblClientes.getSelectedRow();

        txtIdCliMoto.setText(tblClientes.getModel().getValueAt(preencher, 0).toString());
        txtIdCliV.setText(tblClientes.getModel().getValueAt(preencher, 1).toString());

        logicaTblCli();

    }

    private void pesquisarMoto() {

        String sql = "SELECT m.idmoto AS ID, "
                + " c.cliente AS CLIENTE, "
                + " m.modelo AS VEICULO, "
                + " m.placa AS PLACA, m.status AS STATUS, "
                + " DATE_FORMAT(m.data,'%d/%m/%Y - %H:%i') AS DATA, "
                + " m.cor, m.ano "
                + " FROM tbmotos m "
                + " LEFT JOIN tbclientes c ON c.idcli = m.idcli "
                + "WHERE m.modelo LIKE ?";

        try {

            pst = conexao.prepareStatement(sql);

            pst.setString(1, txtPesqMoto.getText() + "%");

            rs = pst.executeQuery();

            // a linha abaixo usa a biblioteca rs2xml.jar para preencher a tabela.
            tblCliMotos.setModel(DbUtils.resultSetToTableModel(rs));

            // Esconde a coluna "cor" (índice 6)
            tblCliMotos.getColumnModel().getColumn(6).setMinWidth(0);
            tblCliMotos.getColumnModel().getColumn(6).setMaxWidth(0);
            tblCliMotos.getColumnModel().getColumn(6).setWidth(0);

            // Esconde a coluna "ano" (índice 7)
            tblCliMotos.getColumnModel().getColumn(7).setMinWidth(0);
            tblCliMotos.getColumnModel().getColumn(7).setMaxWidth(0);
            tblCliMotos.getColumnModel().getColumn(7).setWidth(0);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);

        }

    }

    private void setarCamposTblMoto() {

        int setar = tblCliMotos.getSelectedRow();

        if (setar == -1) {

            return;

        }

        javax.swing.table.TableModel model = tblCliMotos.getModel();

        //Checando se o valor é nulo ANTES de usar .toString() ---
        // (valor == null) ? "" : valor.toString()
        //ISSO EVITA O NULLEXCEPTION <3
        Object idObj = model.getValueAt(setar, 0);
        String id = (idObj == null) ? "" : idObj.toString();

        Object cliObj = model.getValueAt(setar, 1);
        String cliente = (cliObj == null) ? "" : cliObj.toString();

        Object modObj = model.getValueAt(setar, 2);
        String modelo = (modObj == null) ? "" : modObj.toString();

        Object placaObj = model.getValueAt(setar, 3);
        String placa = (placaObj == null) ? "" : placaObj.toString();

        Object statusObj = model.getValueAt(setar, 4);
        String status = (statusObj == null) ? "" : statusObj.toString();

        Object dataObj = model.getValueAt(setar, 5);
        String data = (dataObj == null) ? "" : dataObj.toString();

        Object corObj = model.getValueAt(setar, 6);
        String cor = (corObj == null) ? "" : corObj.toString();

        Object anoObj = model.getValueAt(setar, 7);
        String ano = (anoObj == null) ? "" : anoObj.toString();

        txtIdMoto.setText(id);
        txtIdCliV.setText(cliente);
        txtModeloMoto.setText(modelo);
        txtPlacaMoto.setText(placa);

        txtDataMoto.setText(data);
        txtCorMoto.setText(cor);
        txtAnoMoto.setText(ano);

        logicaTblMoto();

    }

    private void alterarMoto() {

        String sql_valida = "SELECT 1 FROM tbmotos WHERE placa = ? AND idmoto <> ?";

        String sql_alterar = "update tbmotos set idcli = ?, modelo = ?, placa = ?, ano = ?, cor = ?, idusuario =?, data = now() where idmoto = ?";

        try {

            String idCliStr = txtIdCliMoto.getText().toString().trim();
            String modelo = txtModeloMoto.getText().toString().trim();
            String placa = txtPlacaMoto.getText().toString().trim();

            String ano = txtAnoMoto.getText().toString().trim();
            String cor = txtCorMoto.getText().toString().trim();
            String idMotoStr = txtIdMoto.getText().toString().trim();

            if (modelo.isEmpty() || placa.isEmpty() || idCliStr.isEmpty() || idMotoStr.isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                return;
            }

            int idCli;

            int idMoto;

            try {

                idCli = Integer.parseInt(idCliStr);
                idMoto = Integer.parseInt(idMotoStr);

            } catch (NumberFormatException e) {

                JOptionPane.showMessageDialog(null, "Erro: ID do Cliente ou da Moto é inválido.");

                return;
            }

            PreparedStatement pst_valida = conexao.prepareStatement(sql_valida);
            pst_valida.setString(1, placa); // Checa a Placa
            pst_valida.setInt(2, idMoto);
            ResultSet rs_valida = pst_valida.executeQuery();

            if (rs_valida.next()) {
                JOptionPane.showMessageDialog(null, "Atenção: Esta placa já está cadastrada em outra moto!");
                return;
            }

            pst = conexao.prepareStatement(sql_alterar);

            pst.setInt(1, idCli);
            pst.setString(2, modelo);
            pst.setString(3, placa);

            pst.setString(4, ano);
            pst.setString(5, cor);
            pst.setInt(6, TelaPrincipal.idUser);
            pst.setInt(7, idMoto);

            int alterado = pst.executeUpdate();

            if (alterado > 0) {

                JOptionPane.showMessageDialog(null, "Véiculo alterado com sucesso!");

                limparCampos();
                logicaUpdate();
                pesquisarMoto();
                pesquisarCliente();

            } else {

                JOptionPane.showMessageDialog(null, "Nenhuma alteração foi detectada.");
            }

        } catch (Exception e) {

            System.out.println("Erro ao atualizar moto: " + e); // (seu comentário original)
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void inativarMoto() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja INATIVAR este véiculo?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmotos SET status = 'Inativa', idusuario=? WHERE idmoto = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMoto.getText()); // pega o número do ID

                int inativada = pst.executeUpdate();

                if (inativada > 0) {
                    JOptionPane.showMessageDialog(null, "Véiculo inativado com sucesso!");

                    limparCampos();
                    logicaInativar();
                    pesquisarMoto();
                    pesquisarCliente();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarMoto() {

        int ativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja ATIVAR este véiculo?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (ativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbmotos SET status = 'Ativa', idusuario=? WHERE idmoto = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtIdMoto.getText()); // pega o número do ID

                int ativada = pst.executeUpdate();

                if (ativada >= 1) {
                    JOptionPane.showMessageDialog(null, "Véiculo ativado com sucesso!");

                    limparCampos();
                    logicaAtivar();
                    pesquisarMoto();
                    pesquisarCliente();
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void logicaUpdate() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);
        tblClientes.setEnabled(true);
        tblCliMotos.setEnabled(true);
        txtPesqCli.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);
    }

    private void logicaTblCli() {

        btnAdicionar.setEnabled(true);
        txtPesqCli.setEnabled(true);
        tblClientes.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void logicaTblMoto() {

        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);
        tblCliMotos.setEnabled(true);
        txtPesqMoto.setEnabled(true);
        tblClientes.setEnabled(true);
        txtPesqCli.setEnabled(true);

        btnAdicionar.setEnabled(false);

    }

    private void logicaAtivar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        tblCliMotos.setEnabled(true);
        txtPesqCli.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void logicaInativar() {

        btnAdicionar.setEnabled(true);
        btnAlterar.setEnabled(true);

        tblCliMotos.setEnabled(true);
        txtPesqCli.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void logicaPesqMoto() {

        btnAlterar.setEnabled(true);
        btnAtivar.setEnabled(true);
        btnInativar.setEnabled(true);

        txtPesqCli.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAdicionar.setEnabled(false);
        tblCliMotos.setEnabled(false);

    }

    private void limpezaGeralTbl() {

        txtPesqCli.setText(null);
        txtIdCliMoto.setText(null);
        txtIdMoto.setText(null);
        txtIdCliV.setText(null);
        txtDataMoto.setText(null);
        txtModeloMoto.setText(null);
        txtPlacaMoto.setText(null);
        txtAnoMoto.setText(null);
        txtCorMoto.setText(null);

        btnAdicionar.setEnabled(true);

        tblClientes.setEnabled(true);
        tblCliMotos.setEnabled(true);
        txtPesqCli.setEnabled(true);
        txtPesqMoto.setEnabled(true);

        btnAlterar.setEnabled(false);
        btnAtivar.setEnabled(false);
        btnInativar.setEnabled(false);

    }

    private void limparCampos() {

        txtPesqCli.setText(null);
        txtIdCliMoto.setText(null);
        txtIdMoto.setText(null);
        txtIdCliV.setText(null);
        txtDataMoto.setText(null);
        txtModeloMoto.setText(null);
        txtPlacaMoto.setText(null);
        txtAnoMoto.setText(null);
        txtCorMoto.setText(null);

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        btnAdicionar = new javax.swing.JButton();
        txtCorMoto = new javax.swing.JTextField();
        pnlMotos = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblCliMotos = new javax.swing.JTable();
        txtDataMoto = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtIdMoto = new javax.swing.JTextField();
        txtPesqMoto = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        btnInativar = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        txtAnoMoto = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        txtIdCliV = new javax.swing.JTextField();
        btnAtivar = new javax.swing.JButton();
        txtModeloMoto = new javax.swing.JTextField();
        btnLimparDados = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        txtPlacaMoto = new javax.swing.JTextField();
        btnAlterar = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pnlClientes = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientes = new javax.swing.JTable();
        txtPesqCli = new javax.swing.JTextField();
        txtIdCliMoto = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("MOTO GESTOR - CADASTRAR MOTO");
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(850, 600));

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane3.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        btnAdicionar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarDocumento.png"))); // NOI18N
        btnAdicionar.setText("Adicionar");
        btnAdicionar.setToolTipText("Adicionar Véiculo");
        btnAdicionar.setPreferredSize(new java.awt.Dimension(30, 44));
        btnAdicionar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdicionarActionPerformed(evt);
            }
        });

        pnlMotos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlMotos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlMotosMouseClicked(evt);
            }
        });

        tblCliMotos = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblCliMotos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "ID", "CLIENTE", "VÉICULOS", "PLACA", "STATUS", "DATA"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblCliMotos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCliMotosMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblCliMotos);

        txtDataMoto.setEditable(false);
        txtDataMoto.setFont(new java.awt.Font("Tahoma", 1, 9)); // NOI18N

        jLabel2.setText("* ID MOTO:");

        txtIdMoto.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdMoto.setEnabled(false);
        txtIdMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdMotoActionPerformed(evt);
            }
        });

        txtPesqMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesqMotoActionPerformed(evt);
            }
        });
        txtPesqMoto.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqMotoKeyReleased(evt);
            }
        });

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel13.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel13.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel5.setText("MOTO:");

        jLabel7.setText("DATA:");

        javax.swing.GroupLayout pnlMotosLayout = new javax.swing.GroupLayout(pnlMotos);
        pnlMotos.setLayout(pnlMotosLayout);
        pnlMotosLayout.setHorizontalGroup(
            pnlMotosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(pnlMotosLayout.createSequentialGroup()
                .addGroup(pnlMotosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMotosLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtIdMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDataMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlMotosLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPesqMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlMotosLayout.setVerticalGroup(
            pnlMotosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMotosLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(pnlMotosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPesqMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(13, 13, 13)
                .addGroup(pnlMotosLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtIdMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDataMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7))
                .addGap(11, 11, 11)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE))
        );

        btnInativar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao.png"))); // NOI18N
        btnInativar.setText("Inativar");
        btnInativar.setToolTipText("Inativar Véiculo");
        btnInativar.setEnabled(false);
        btnInativar.setPreferredSize(new java.awt.Dimension(30, 44));
        btnInativar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarActionPerformed(evt);
            }
        });

        jLabel10.setText("CLIENTE:");

        jLabel9.setText("COR:");

        txtIdCliV.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdCliV.setEnabled(false);
        txtIdCliV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdCliVActionPerformed(evt);
            }
        });

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar Véiculo");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(30, 44));
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

        jLabel6.setText("* PLACA:");

        btnAlterar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarDocumento.png"))); // NOI18N
        btnAlterar.setText("Editar");
        btnAlterar.setToolTipText("Editar Dados");
        btnAlterar.setEnabled(false);
        btnAlterar.setPreferredSize(new java.awt.Dimension(30, 44));
        btnAlterar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarActionPerformed(evt);
            }
        });

        jLabel8.setText("* MODELO:");

        jLabel4.setText("ANO FABRICAÇÃO:");

        pnlClientes.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        pnlClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlClientesMouseClicked(evt);
            }
        });

        jLabel1.setText("* ID CLIENTE:");

        tblClientes = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }

        };
        tblClientes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "NOME", "TELEFONE"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblClientes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClientes);

        txtPesqCli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPesqCliActionPerformed(evt);
            }
        });
        txtPesqCli.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtPesqCliKeyReleased(evt);
            }
        });

        txtIdCliMoto.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        txtIdCliMoto.setEnabled(false);
        txtIdCliMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIdCliMotoActionPerformed(evt);
            }
        });

        jLabel11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        jLabel11.setText("* CAMPOS OBRIGATÓRIOS");
        jLabel11.setPreferredSize(new java.awt.Dimension(25, 25));

        jLabel14.setText("CLIENTE:");

        javax.swing.GroupLayout pnlClientesLayout = new javax.swing.GroupLayout(pnlClientes);
        pnlClientes.setLayout(pnlClientesLayout);
        pnlClientesLayout.setHorizontalGroup(
            pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlClientesLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtPesqCli, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIdCliMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlClientesLayout.setVerticalGroup(
            pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClientesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtPesqCli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIdCliMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCorMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtModeloMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 426, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtAnoMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel10))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtIdCliV, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPlacaMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 65, Short.MAX_VALUE)
                        .addComponent(btnAlterar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(72, 72, 72)
                        .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(70, 70, 70)
                        .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)))
                .addGap(335, 335, 335))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(pnlClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlMotos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(323, 323, 323))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(pnlClientes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdCliV, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10)))
                    .addComponent(pnlMotos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPlacaMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtModeloMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCorMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtAnoMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(37, 37, 37)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAdicionar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        jScrollPane3.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3)
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void pnlClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlClientesMouseClicked

        tblClientes.clearSelection();

        limpezaGeralTbl();
    }//GEN-LAST:event_pnlClientesMouseClicked

    private void txtIdCliMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdCliMotoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdCliMotoActionPerformed

    private void txtPesqCliKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqCliKeyReleased
        pesquisarCliente();
    }//GEN-LAST:event_txtPesqCliKeyReleased

    private void txtPesqCliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqCliActionPerformed

    }//GEN-LAST:event_txtPesqCliActionPerformed

    private void tblClientesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesMouseClicked

        preencherTblCli();
    }//GEN-LAST:event_tblClientesMouseClicked

    private void btnAlterarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarActionPerformed
        alterarMoto();
    }//GEN-LAST:event_btnAlterarActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarMoto();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void txtIdCliVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdCliVActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIdCliVActionPerformed

    private void btnInativarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarActionPerformed
        inativarMoto();
    }//GEN-LAST:event_btnInativarActionPerformed

    private void pnlMotosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlMotosMouseClicked

        tblCliMotos.clearSelection();
        limpezaGeralTbl();
    }//GEN-LAST:event_pnlMotosMouseClicked

    private void txtPesqMotoKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPesqMotoKeyReleased
        pesquisarMoto();
    }//GEN-LAST:event_txtPesqMotoKeyReleased

    private void txtPesqMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPesqMotoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPesqMotoActionPerformed

    private void txtIdMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIdMotoActionPerformed

    }//GEN-LAST:event_txtIdMotoActionPerformed

    private void tblCliMotosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblCliMotosMouseClicked

        setarCamposTblMoto();
    }//GEN-LAST:event_tblCliMotosMouseClicked

    private void btnAdicionarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdicionarActionPerformed
        adicionarMoto();
    }//GEN-LAST:event_btnAdicionarActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdicionar;
    private javax.swing.JButton btnAlterar;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnInativar;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel pnlClientes;
    private javax.swing.JPanel pnlMotos;
    private javax.swing.JTable tblCliMotos;
    private javax.swing.JTable tblClientes;
    private javax.swing.JTextField txtAnoMoto;
    private javax.swing.JTextField txtCorMoto;
    private javax.swing.JTextField txtDataMoto;
    private javax.swing.JTextField txtIdCliMoto;
    private javax.swing.JTextField txtIdCliV;
    private javax.swing.JTextField txtIdMoto;
    private javax.swing.JTextField txtModeloMoto;
    private javax.swing.JTextField txtPesqCli;
    private javax.swing.JTextField txtPesqMoto;
    private javax.swing.JTextField txtPlacaMoto;
    // End of variables declaration//GEN-END:variables
}

package br.com.motogestor.telas;

import java.sql.*;
import br.com.motogestor.DAL.ModuloConexao;
import br.com.motogestor.telas.SubTelaPesqProduto;
import br.com.motogestor.telas.SubTelaPesqServ;
import br.com.motogestor.telas.SubTelaPesqMoto;
import br.com.motogestor.telas.SubTelaPesqMecanico;
import br.com.motogestor.DAL.ModuloConexao;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.DefaultTableModel;
import net.proteanit.sql.DbUtils;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.view.JasperViewer;

public class TelaOS extends javax.swing.JInternalFrame {

    Connection conexao = null;
    PreparedStatement pst = null;
    ResultSet rs = null;
    
    
    private Integer mecIdSelecionado = null;
    
    private int idUser;

    private String tipo;

    public TelaOS(int idUser) {

        initComponents();

        ButtonGroup grupoTipo = new ButtonGroup();
        grupoTipo.add(rbtOrc);
        grupoTipo.add(rbtOs);

        conexao = ModuloConexao.conector();
        this.idUser = idUser;
        configurarTabelas();
        pesquisarCliente();

    }
    
    

    private void pesquisarCliente() {

        String sql = "select idcli as ID, cliente as NOME, telefone as TELEFONE from tbclientes where cliente like ?";

        try {

            pst = conexao.prepareStatement(sql);
            pst.setString(1, txtCliPesquisar.getText() + "%");

            rs = pst.executeQuery();
            tblClientesOs.setModel(DbUtils.resultSetToTableModel(rs));

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void preencherCamposOs() {

        int preencher = tblClientesOs.getSelectedRow();
        txtCliIdOs.setText(tblClientesOs.getModel().getValueAt(preencher, 0).toString());

    }

    public void setMecSelecionado(Integer id, String nome) {

        this.mecIdSelecionado = id;

        if (nome != null) {

            txtOsResp.setText(nome);

        }
    }

    private void emitirOS() {
        String sql = "INSERT INTO tbos (tipo, situacao, motocicleta, placa, defeito, tecnico, valor, idcli, "
                + "statusos, idusuario, id_meca) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'Ativa', ?, ?)";

        try {

            conexao.setAutoCommit(false);

            if (txtCliIdOs.getText().trim().isEmpty() || txtOsMoto.getText().trim().isEmpty()
                    || txtOsDef.getText().trim().isEmpty() || cboOsSit.getSelectedItem().toString().trim().equals(" ")
                    || txtOsValor.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");
                conexao.setAutoCommit(true);
                return;
            }

            pst = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, tipo);
            pst.setString(2, cboOsSit.getSelectedItem().toString().trim());
            pst.setString(3, txtOsMoto.getText().trim());
            pst.setString(4, txtPlaca.getText().trim());

            pst.setString(5, txtOsDef.getText().trim());
            pst.setString(6, txtOsResp.getText().trim());
            pst.setString(7, txtOsValor.getText().replace(",", ".").trim());
            pst.setString(8, txtCliIdOs.getText().trim());
            pst.setInt(9, TelaPrincipal.idUser);

            if (mecIdSelecionado != null) {

                pst.setInt(10, mecIdSelecionado);

            } else {
                pst.setNull(10, java.sql.Types.INTEGER);
            }

            int inserido = pst.executeUpdate();

            if (inserido <= 0) {
                conexao.rollback();
                JOptionPane.showMessageDialog(null, "Erro ao criar OS.");
                conexao.setAutoCommit(true);
                return;
            }

            ResultSet rs = pst.getGeneratedKeys();

            int idOSGerada = 0;

            if (rs.next()) {

                idOSGerada = rs.getInt(1);
                txtOs.setText(String.valueOf(idOSGerada));
            }

            rs.close();

            // Primeiro: validar estoque para todos os itens (faz lock das linhas com FOR UPDATE)
            DefaultTableModel model = (DefaultTableModel) tblProdutosOS.getModel();

            // Map para guardar pares (produtoId -> quantidade a retirar) se precisar
            for (int i = 0; i < model.getRowCount(); i++) {

                Object produtoObj = model.getValueAt(i, 0);
                Object qtdObj = model.getValueAt(i, 1);

                if (produtoObj == null) {
                    continue;
                }

                String produto = produtoObj.toString();
                double qtd = Double.parseDouble(qtdObj.toString());

                int prodId = buscarIdProduto(produto);

                Double estoqueAtual = getQuantidadeEstoqueForUpdate(prodId);
                if (estoqueAtual == null) {

                    conexao.rollback();

                    JOptionPane.showMessageDialog(null, "Produto " + produto + " sem registro de estoque. Operação cancelada.");

                    conexao.setAutoCommit(true);
                    return;
                }

                if (estoqueAtual < qtd) {

                    conexao.rollback();

                    JOptionPane.showMessageDialog(null, "Estoque insuficiente para o produto " + produto
                            + ". Disponível: " + estoqueAtual + ", necessário: " + qtd + ". Operação cancelada.");
                    conexao.setAutoCommit(true);
                    return;
                }

            }

            for (int i = 0; i < model.getRowCount(); i++) {
                Object produtoObj = model.getValueAt(i, 0);
                Object qtdObj = model.getValueAt(i, 1);
                Object valorUnitObj = model.getValueAt(i, 2);
                Object valorTotObj = model.getValueAt(i, 3);

                if (produtoObj == null) {

                    continue;
                }

                String produto = produtoObj.toString();
                int qtd = Integer.parseInt(qtdObj.toString());
                double valorUnit = Double.parseDouble(valorUnitObj.toString());
                double valorTot = Double.parseDouble(valorTotObj.toString());

                int prodId = buscarIdProduto(produto);

                PreparedStatement pstItens = conexao.prepareStatement(
                        "INSERT INTO tbos_itens (os_id, cod_produto, quantidade, valor_unit, valor_total) VALUES (?, ?, ?, ?, ?)"
                );

                pstItens.setInt(1, idOSGerada);
                pstItens.setInt(2, prodId);
                pstItens.setInt(3, qtd);
                pstItens.setDouble(4, valorUnit);
                pstItens.setDouble(5, valorTot);

                pstItens.executeUpdate();
                pstItens.close();

                // Atualiza o estoque com UPDATE (subtrai a quantidade)
                boolean atualizou = decrementarEstoque(prodId, qtd);
                if (!atualizou) {

                    conexao.rollback();

                    JOptionPane.showMessageDialog(null, "Erro ao atualizar estoque para o produto " + produto + ". Operação cancelada.");

                    conexao.setAutoCommit(true);
                    return;
                }
            }

            DefaultTableModel modelServ = (DefaultTableModel) tblServicosOS.getModel();

            for (int i = 0; i < modelServ.getRowCount(); i++) {
                Object servicoObj = modelServ.getValueAt(i, 0);
                Object valorObj = modelServ.getValueAt(i, 1);

                if (servicoObj == null) {

                    continue;
                }

                String servico = servicoObj.toString();
                double valor = Double.parseDouble(valorObj.toString());

                PreparedStatement pstServ = conexao.prepareStatement(
                        "INSERT INTO tbos_servicos (os_id, cod_servico, descricao, valor) VALUES (?, ?, ?, ?)"
                );

                pstServ.setInt(1, idOSGerada);
                pstServ.setInt(2, buscarIdServico(servico));
                pstServ.setString(3, servico);
                pstServ.setDouble(4, valor);

                pstServ.executeUpdate();
                pstServ.close();
            }

            conexao.commit();
            JOptionPane.showMessageDialog(null, "OS emitida com sucesso!\nNº OS: " + idOSGerada);

            limparCampos();

        } catch (Exception e) {

            try {

                conexao.rollback();

            } catch (Exception exRollback) {

                exRollback.printStackTrace();
            }

            JOptionPane.showMessageDialog(null, "Erro ao emitir OS: " + e.getMessage());

            e.printStackTrace();

        } finally {

            try {
                conexao.setAutoCommit(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Retorna a quantidade atual em estoque para um produto, usando SELECT ...
     * FOR UPDATE. Deve ser chamado dentro de uma transação (autoCommit = false)
     * para bloquear a linha. Retorna null se não existir registro no tbestoque
     * para o produto.
     */
    private Double getQuantidadeEstoqueForUpdate(int codProduto) throws SQLException {

        String sql = "SELECT quantidade FROM tbestoque WHERE codproduto = ? FOR UPDATE";
        try (PreparedStatement pst = conexao.prepareStatement(sql)) {
            pst.setInt(1, codProduto);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("quantidade");
                } else {
                    return null; // sem registro
                }
            }
        }
    }

    /**
     * Decrementa (subtrai) a quantidade do estoque com um UPDATE. Retorna true
     * se a atualização afetou 1 linha, false caso contrário.
     */
    private boolean decrementarEstoque(int codProduto, double quantidade) throws SQLException {

        String sqlUpdate = "UPDATE tbestoque SET quantidade = quantidade - ?, dtatualizacao = ? WHERE codproduto = ?";

        try (PreparedStatement pst = conexao.prepareStatement(sqlUpdate)) {

            pst.setDouble(1, quantidade);
            pst.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
            pst.setInt(3, codProduto);

            int affected = pst.executeUpdate();
            return affected == 1;
        }
    }

    private int buscarIdProduto(String nomeProduto) throws SQLException {

        String sql = "SELECT idproduto FROM tbprodutos WHERE produto = ?";

        PreparedStatement pstBusca = conexao.prepareStatement(sql);
        pstBusca.setString(1, nomeProduto);

        ResultSet rs = pstBusca.executeQuery();
        int id = 0;

        if (rs.next()) {

            id = rs.getInt(1);
        }

        rs.close();
        pstBusca.close();
        return id;
    }

    private int buscarIdServico(String nomeServico) throws SQLException {

        String sql = "SELECT idservico FROM tbservicos WHERE servico = ?";

        PreparedStatement pstBusca = conexao.prepareStatement(sql);
        pstBusca.setString(1, nomeServico);
        ResultSet rs = pstBusca.executeQuery();

        int id = 0;

        if (rs.next()) {

            id = rs.getInt(1);
        }

        rs.close();
        pstBusca.close();

        return id;
    }

    private void carregarProdutosDaOS(int osId) {

        String sql = "SELECT p.produto, i.quantidade, i.valor_unit, i.valor_total "
                + "FROM tbos_itens i "
                + "JOIN tbprodutos p ON p.idproduto = i.cod_produto "
                + "WHERE i.os_id = ?";

        try {

            PreparedStatement pstProd = conexao.prepareStatement(sql);
            pstProd.setInt(1, osId);
            ResultSet rsProd = pstProd.executeQuery();

            DefaultTableModel modelProd = (DefaultTableModel) tblProdutosOS.getModel();
            modelProd.setRowCount(0);

            while (rsProd.next()) {

                modelProd.addRow(new Object[]{
                    rsProd.getString("produto"),
                    rsProd.getInt("quantidade"),
                    rsProd.getDouble("valor_unit"),
                    rsProd.getDouble("valor_total")

                });
            }

            rsProd.close();
            pstProd.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void carregarServicosDaOS(int osId) {

        String sql = "SELECT s.servico, i.valor "
                + "FROM tbos_servicos i "
                + "JOIN tbservicos s ON s.idservico = i.cod_servico "
                + "WHERE i.os_id = ?";

        try {

            PreparedStatement pstServ = conexao.prepareStatement(sql);
            pstServ.setInt(1, osId);
            ResultSet rsServ = pstServ.executeQuery();

            DefaultTableModel modelServ = (DefaultTableModel) tblServicosOS.getModel();
            modelServ.setRowCount(0);

            while (rsServ.next()) {

                modelServ.addRow(new Object[]{
                    rsServ.getString("servico"),
                    rsServ.getDouble("valor")

                });
            }

            rsServ.close();
            pstServ.close();

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(null, "Erro ao carregar serviços: " + e.getMessage());
        }
    }

    private void alterarOS() {

        String sql = "UPDATE tbos SET tipo = ?, situacao = ?, motocicleta = ?, placa = ?, defeito = ?, "
                + "tecnico = ?, valor = ?, idusuario = ?, data = now(), id_meca = ? WHERE os = ?";

        try {
            //começo da transação
            conexao.setAutoCommit(false);

            if (txtOs.getText().trim().isEmpty() || txtOsMoto.getText().trim().isEmpty()
                    || txtOsDef.getText().trim().isEmpty() || cboOsSit.getSelectedItem().toString().trim().equals(" ")
                    || txtOsResp.getText().trim().isEmpty() || txtOsValor.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(null, "Preencha todos os campos obrigatórios!");

                conexao.setAutoCommit(true);

                return;
            }

            int idOS = Integer.parseInt(txtOs.getText().trim());

            //CARREGAR ITENS ANTIGOS (map: prodId -> qtdAntiga)
            Map<Integer, Double> mapAntigos = new java.util.HashMap<>();

            String sqlBuscaItens = "SELECT cod_produto, quantidade FROM tbos_itens WHERE os_id = ?";

            try (PreparedStatement pstBusca = conexao.prepareStatement(sqlBuscaItens)) {

                pstBusca.setInt(1, idOS);

                try (ResultSet rsItensAntigos = pstBusca.executeQuery()) {

                    while (rsItensAntigos.next()) {

                        int codProdutoAntigo = rsItensAntigos.getInt("cod_produto");

                        double qtdAntiga = rsItensAntigos.getDouble("quantidade");

                        //soma se houver duplicatas (por segurança)
                        mapAntigos.put(codProdutoAntigo, mapAntigos.getOrDefault(codProdutoAntigo, 0.0) + qtdAntiga);
                    }
                }
            }

            //MONTAR ITENS NOVOS A PARTIR DA TABELA (map: prodId -> qtdNova)
            Map<Integer, Double> mapNovos = new java.util.HashMap<>();

            DefaultTableModel modelProd = (DefaultTableModel) tblProdutosOS.getModel();

            for (int i = 0; i < modelProd.getRowCount(); i++) {

                Object prodObj = modelProd.getValueAt(i, 0);
                Object qtdObj = modelProd.getValueAt(i, 1);

                if (prodObj == null) {

                    continue;
                }

                String produto = prodObj.toString();
                double qtd = 0;

                try {

                    qtd = Double.parseDouble(qtdObj.toString());

                } catch (Exception ex) {

                    conexao.rollback(); // Cancela a transação
                    JOptionPane.showMessageDialog(null, "Quantidade inválida para o produto: " + produto);
                    conexao.setAutoCommit(true); // Retorna ao normal

                    return;
                }

                int prodId = buscarIdProduto(produto);
                mapNovos.put(prodId, mapNovos.getOrDefault(prodId, 0.0) + qtd);
            }

            //CALCULAR DELTAS (novo - antigo) e APLICAR SOMENTE A DIFERENÇA
            // Se delta > 0  -> reduzir estoque (mais saída)
            // Se delta < 0  -> aumentar estoque (devolução)
            // use um conjunto com todas as chaves
            java.util.Set<Integer> allKeys = new java.util.HashSet<>();
            allKeys.addAll(mapAntigos.keySet());
            allKeys.addAll(mapNovos.keySet());

            //VALIDAÇÃO DE ESTOQUE: verifica para TODOS os produtos com delta>0
          
            for (Integer prodId : allKeys) {

                double antiga = mapAntigos.getOrDefault(prodId, 0.0);
                double nova = mapNovos.getOrDefault(prodId, 0.0);
                double delta = nova - antiga;

                if (delta <= 0.0) {

                    continue;
                }

                Double saldoAtual = getQuantidadeEstoqueForUpdate(prodId); // seu método já retorna null se não existir
                if (saldoAtual == null) {

                    conexao.rollback();

                    JOptionPane.showMessageDialog(null, "Produto ID " + prodId + " sem registro de saldo em estoque. Alteração cancelada.");

                    conexao.setAutoCommit(true);
                    return;
                }

                if (saldoAtual < delta) {

                    conexao.rollback();

                    JOptionPane.showMessageDialog(null,
                            "Estoque insuficiente para produto ID " + prodId + ". Disponível: " + saldoAtual + ", necessário: " + delta + ". Alteração cancelada.");

                    conexao.setAutoCommit(true);

                    return;
                }
            }

            for (Integer prodId : allKeys) {

                double antiga = mapAntigos.getOrDefault(prodId, 0.0);
                double nova = mapNovos.getOrDefault(prodId, 0.0);
                double delta = nova - antiga; // >0 precisa tirar delta do estoque; <0 precisa devolver -delta

                if (delta == 0.0) {
                    continue; // nada a fazer
                }
                if (delta > 0) {

                    // precisa reduzir estoque em delta
                    boolean ok = aplicarReducaoEstoque(prodId, delta);

                    if (!ok) {

                        conexao.rollback();

                        JOptionPane.showMessageDialog(null, "Erro ao reduzir estoque do produto ID " + prodId + ". Alteração cancelada.");

                        conexao.setAutoCommit(true);
                        return;
                    }
                } else { // delta < 0 -> devolver |delta|

                    boolean ok = aplicarDevolucaoEstoque(prodId, -delta);
                    if (!ok) {

                        conexao.rollback();

                        JOptionPane.showMessageDialog(null, "Erro ao devolver estoque do produto ID " + prodId + ". Alteração cancelada.");

                        conexao.setAutoCommit(true);
                        return;
                    }
                }
            }

            pst = conexao.prepareStatement(sql);
            pst.setString(1, tipo);
            pst.setString(2, cboOsSit.getSelectedItem().toString());
            pst.setString(3, txtOsMoto.getText().trim());
            pst.setString(4, txtPlaca.getText().trim());
            pst.setString(5, txtOsDef.getText().trim());
            pst.setString(6, txtOsResp.getText().trim());
            pst.setString(7, txtOsValor.getText().replace(",", ".").trim());
            pst.setInt(8, TelaPrincipal.idUser);

            if (mecIdSelecionado != null) {

                pst.setInt(9, mecIdSelecionado);
            } else {

                pst.setNull(9, java.sql.Types.INTEGER);
            }

            pst.setInt(10, idOS);

            int alterado = pst.executeUpdate();

            if (alterado <= 0) {

                conexao.rollback();
                JOptionPane.showMessageDialog(null, "Erro ao atualizar OS. Operação cancelada.");
                conexao.setAutoCommit(true);
                return;
            }

            //APAGAR ITENS E SERVIÇOS ANTIGOS
            try (PreparedStatement pstDelProd = conexao.prepareStatement("DELETE FROM tbos_itens WHERE os_id = ?")) {

                pstDelProd.setInt(1, idOS);
                pstDelProd.executeUpdate();
            }

            try (PreparedStatement pstDelServ = conexao.prepareStatement("DELETE FROM tbos_servicos WHERE os_id = ?")) {

                pstDelServ.setInt(1, idOS);
                pstDelServ.executeUpdate();
            }

            //REINSERIR ITENS (sem tocar estoque — já ajustado pelas deltas)
            for (int i = 0; i < modelProd.getRowCount(); i++) {

                Object prodObj = modelProd.getValueAt(i, 0);
                Object qtdObj = modelProd.getValueAt(i, 1);
                Object valUnitObj = modelProd.getValueAt(i, 2);
                Object valTotObj = modelProd.getValueAt(i, 3);

                if (prodObj == null) {

                    continue;
                }

                String produto = prodObj.toString();
                int qtd = Integer.parseInt(qtdObj.toString());
                double valorUnit = Double.parseDouble(valUnitObj.toString());
                double valorTot = Double.parseDouble(valTotObj.toString());

                int prodId = buscarIdProduto(produto);

                try (PreparedStatement pstProd = conexao.prepareStatement(
                        "INSERT INTO tbos_itens (os_id, cod_produto, quantidade, valor_unit, valor_total) VALUES (?, ?, ?, ?, ?)")) {

                    pstProd.setInt(1, idOS);
                    pstProd.setInt(2, prodId);
                    pstProd.setInt(3, qtd);
                    pstProd.setDouble(4, valorUnit);
                    pstProd.setDouble(5, valorTot);
                    pstProd.executeUpdate();
                }
            }

            //REINSERIR SERVIÇOS
            DefaultTableModel modelServ = (DefaultTableModel) tblServicosOS.getModel();
            for (int i = 0; i < modelServ.getRowCount(); i++) {
                
                Object servObj = modelServ.getValueAt(i, 0);
                Object valObj = modelServ.getValueAt(i, 1);

                if (servObj == null) {

                    continue;
                }

                String servico = servObj.toString();
                double valor = Double.parseDouble(valObj.toString());

                try (PreparedStatement pstServ = conexao.prepareStatement(
                        "INSERT INTO tbos_servicos (os_id, cod_servico, descricao, valor) VALUES (?, ?, ?, ?)")) {
                    pstServ.setInt(1, idOS);
                    pstServ.setInt(2, buscarIdServico(servico));
                    pstServ.setString(3, servico);
                    pstServ.setDouble(4, valor);
                    pstServ.executeUpdate();
                }
            }

            conexao.commit();
            JOptionPane.showMessageDialog(null, "OS atualizada com sucesso!");
            limparCampos();

        } catch (Exception e) {
            
            try {
                conexao.rollback();
            } catch (Exception exRollback) {
                
                exRollback.printStackTrace();
            }
            
            JOptionPane.showMessageDialog(null, "Erro ao alterar OS: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            
            try {
                conexao.setAutoCommit(true);
                
            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }

    /**
     * Tenta reduzir o estoque (subtrair). Primeiro tenta atualizar o saldo
     * (UPDATE). Se não afetar linhas (nenhum saldo registrado), insere um
     * registro de saída (quantidade negativa).
     */
    
    private boolean aplicarReducaoEstoque(int codProduto, double quantidade) {

        try {

            //Tenta atualizar saldo
            String sqlUpdate = "UPDATE tbestoque SET quantidade = quantidade - ?, dtatualizacao = ? WHERE codproduto = ?";

            try (PreparedStatement pst = conexao.prepareStatement(sqlUpdate)) {

                pst.setDouble(1, quantidade);
                pst.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                pst.setInt(3, codProduto);

                int afetada = pst.executeUpdate();

                if (afetada == 1) {
                    return true;
                }
            }

            //Fallback: insere movimento negativo (saída)
            String sqlMov = "INSERT INTO tbestoque (codproduto, quantidade, dtatualizacao) VALUES (?, ?, ?)";
            try (PreparedStatement pst2 = conexao.prepareStatement(sqlMov)) {

                pst2.setInt(1, codProduto);
                pst2.setDouble(2, -quantidade); // negativo = saída
                pst2.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

                pst2.executeUpdate();
                return true;
            }

        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    /**
     * Tenta aumentar o estoque (devolução). Primeiro tenta atualizar saldo
     * (UPDATE). Se não afetar linhas, insere um movimento positivo (entrada).
     */
    
    private boolean aplicarDevolucaoEstoque(int codProduto, double quantidade) {

        try {
            //Tenta atualizar saldo (adiciona quantidade)
            String sqlUpdateAdd = "UPDATE tbestoque SET quantidade = quantidade + ?, dtatualizacao = ? WHERE codproduto = ?";

            try (PreparedStatement pst = conexao.prepareStatement(sqlUpdateAdd)) {

                pst.setDouble(1, quantidade);
                pst.setTimestamp(2, new java.sql.Timestamp(System.currentTimeMillis()));
                pst.setInt(3, codProduto);

                int affected = pst.executeUpdate();

                if (affected == 1) {
                    return true;
                }
            }

            //Fallback: insere movimento positivo (entrada)
            String sqlMov = "INSERT INTO tbestoque (codproduto, quantidade, dtatualizacao) VALUES (?, ?, ?)";
            try (PreparedStatement pst2 = conexao.prepareStatement(sqlMov)) {
                pst2.setInt(1, codProduto);
                pst2.setDouble(2, quantidade); // positivo = entrada
                pst2.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));

                pst2.executeUpdate();

                return true;
            }
        } catch (Exception e) {

            e.printStackTrace();

            return false;
        }
    }

    private void inativarOs() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja INATIVAR esta OS?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbos SET statusos = 'Inativa', idusuario=? WHERE os = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtOs.getText()); // pega o número da OS

                int osInativada = pst.executeUpdate();

                if (osInativada > 0) {

                    JOptionPane.showMessageDialog(null, "OS inativada com sucesso!");

                    limparCampos();
                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void ativarOS() {

        int inativacao = JOptionPane.showConfirmDialog(null,
                "Tem certeza que deseja ATIVAR esta OS?",
                "Atenção",
                JOptionPane.YES_NO_OPTION);

        if (inativacao == JOptionPane.YES_NO_OPTION) {

            String sql = "UPDATE tbos SET statusos = 'Ativa', idusuario=? WHERE os = ?";

            try {

                pst = conexao.prepareStatement(sql);
                pst.setInt(1, TelaPrincipal.idUser);
                pst.setString(2, txtOs.getText()); // pega o número da OS

                int osAtivada = pst.executeUpdate();

                if (osAtivada > 0) {

                    JOptionPane.showMessageDialog(null, "OS ativada com sucesso!");

                    limparCampos();

                }

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    private void imprimirOS() {

        // IMPRIMINDO UMA OS
        int confirmacao = JOptionPane.showConfirmDialog(null, "Confirma a impressão dessa OS? ", "Atenção", JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_NO_OPTION) {

            //imprimindo relatório com o framework JasperReports
            try {

                //USANDO A CLASSE HashMap P PARA CRIAR UM FILTRO
                HashMap filtro = new HashMap();

                filtro.put("Os_2", Integer.parseInt(txtOs.getText()));

                //usando a classe JasperPrint para preparar a impressão de um relatório.
                JasperPrint impressao = JasperFillManager.fillReport("C:\\PLAYLIST JOSE DE ASSIS JAVASQL\\IREPORT (RELATÓRIOS)\\reports (relatórios criados pelo ireport)\\Os_2.jasper", filtro, conexao);

                // a linha abaixo exibe o relatório
                JasperViewer.viewReport(impressao, false);

                limparCampos();

            } catch (Exception e) {

                JOptionPane.showMessageDialog(null, e);
            }

        }

    }

    //RECUPERAR OS QUE É GERADA PELO AUTO INCREMENT NO SQL
    private void recuperarOS() {

        String sql = "select max(os) from tbos";

        try {

            pst = conexao.prepareStatement(sql);
            rs = pst.executeQuery();

            if (rs.next()) {

                txtOs.setText(rs.getString(1));

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, e);
        }

    }

    private void pesquisarMecanico() {

        try {

            SubTelaPesqMecanico mecanico = new SubTelaPesqMecanico(null, true, this);
            mecanico.setLocationRelativeTo(this);// true = modal
            mecanico.setVisible(true);

            // se algum serviço foi selecionado, preenche os campos da OS
            if (mecanico.mecanicoSelecionado != null) {

                txtOsResp.setText(mecanico.mecanicoSelecionado);

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar a marca! " + e);

        } finally {

            //fecharConexao();
        }

    }

    private void pesquisarServico() {

        try {

            SubTelaPesqServ servico = new SubTelaPesqServ(null, true);
            servico.setVisible(true);

            if (servico.servicoSelecionado != null && servico.valorSelecionado != null) {

                DefaultTableModel model = (DefaultTableModel) tblServicosOS.getModel();
                model.addRow(new Object[]{
                    servico.servicoSelecionado,
                    Double.parseDouble(servico.valorSelecionado.replace(",", "."))

                });

                somarTabelaServicos();
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar serviço! " + e);
        }
    }

    private void pesquisarOS() {

        try {

            SubTelaPesqOs sub = new SubTelaPesqOs(null, true);
            sub.setVisible(true);

            if (sub.osSel != null) {

                txtOs.setText(sub.osSel);
                txtData.setText(sub.dataSel);
                txtOsMoto.setText(sub.motoSel);
                txtPlaca.setText(sub.placaSel);
                txtOsDef.setText(sub.defeitoSel);
                txtOsResp.setText(sub.tecnicoSel);
                txtStatusOs.setText(sub.statusSel);
                txtCliIdOs.setText(sub.idcliSel);

                try {

                    // Troca vírgula por ponto caso venha formatado errado do banco
                    String valorTratado = sub.valorSel.replace(",", ".");
                    double valorDouble = Double.parseDouble(valorTratado);

                    txtOsValor.setText(String.format("%.2f", valorDouble));

                } catch (Exception e) {

                    txtOsValor.setText("0,00");
                }

                cboOsSit.setSelectedItem(sub.situacaoSel);

                if ("OS".equalsIgnoreCase(sub.tipoSel)) {

                    rbtOs.setSelected(true);
                    tipo = "OS";

                } else {

                    rbtOrc.setSelected(true);
                    tipo = "Orçamento";
                }

                String sqlCli = "SELECT idcli, cliente, telefone FROM tbclientes WHERE idcli = ?";
                PreparedStatement pstCli = conexao.prepareStatement(sqlCli);
                pstCli.setString(1, sub.idcliSel);
                ResultSet rsCli = pstCli.executeQuery();

                if (rsCli.next()) {
                    DefaultTableModel modelCli = (DefaultTableModel) tblClientesOs.getModel();
                    modelCli.setRowCount(0);
                    modelCli.addRow(new Object[]{
                        rsCli.getInt("idcli"),
                        rsCli.getString("cliente"),
                        rsCli.getString("telefone")
                    });
                }

                rsCli.close();
                pstCli.close();

                //Carregar Tabelas de Itens (Produtos e Serviços)
                carregarProdutosDaOS(Integer.parseInt(sub.osSel));
                carregarServicosDaOS(Integer.parseInt(sub.osSel));

                somarTabelaProdutos();
                somarTabelaServicos();

                btnGerarOs.setEnabled(false);
                txtCliPesquisar.setEnabled(false);
                btnPesquisarOs.setEnabled(false);

                btnAtivar.setEnabled(true);
                btnAlterarOs.setEnabled(true);
                btnInativarOs.setEnabled(true);
                btnEmitirOs.setEnabled(true);
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar OS, tente novamente! " + e);

        }
    }

    private void pesquisarProduto() {

        try {

            SubTelaPesqProduto produto = new SubTelaPesqProduto(null, true); // true = modal
            produto.setVisible(true);

            if (produto.produtoSelecionado != null && produto.valorSelecionado != null) {

                String nomeProduto = produto.produtoSelecionado;
                String valorStr = produto.valorSelecionado.trim();

                if (valorStr.isEmpty()) {

                    JOptionPane.showMessageDialog(null, "Valor do produto não informado!");

                    return;
                }

                double valorUnitario = Double.parseDouble(valorStr.replace(",", "."));
                int quantidade = 1;
                double total = valorUnitario * quantidade;

                DefaultTableModel model = (DefaultTableModel) tblProdutosOS.getModel();
                model.addRow(new Object[]{
                    nomeProduto,
                    quantidade,
                    valorUnitario,
                    total
                });

                somarTabelaProdutos();
            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar o produto! " + e);
        }
    }

    private void pesquisarMoto() {

        try {

            SubTelaPesqMoto moto = new SubTelaPesqMoto(null, true); // true = modal
            moto.setVisible(true);

            if (moto.motoSelecionada != null) {

                txtOsMoto.setText(moto.motoSelecionada);
                txtPlaca.setText(moto.placaSelecionada);

                txtCliIdOs.setText(moto.idClienteSelecionado);

                DefaultTableModel modelCliente = (DefaultTableModel) tblClientesOs.getModel();
                modelCliente.setRowCount(0); // Limpa a tabela

                // Adiciona o cliente (dono da moto) na tabela
                modelCliente.addRow(new Object[]{
                    moto.idClienteSelecionado,
                    moto.nomeClienteSelecionado,
                    moto.foneClienteSelecionado

                });
                // -----------------------------------------------------------------

            }

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Erro ao pesquisar o veículo! " + e);
            e.printStackTrace();
        }
    }

    private void somarTabelaProdutos() {

        double soma = 0.0;

        DefaultTableModel model = (DefaultTableModel) tblProdutosOS.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {

            Object qtdObj = model.getValueAt(i, 1);
            Object unitObj = model.getValueAt(i, 2);

            if (qtdObj != null && unitObj != null) {

                try {

                    int qtd = Integer.parseInt(qtdObj.toString());
                    double unit = Double.parseDouble(unitObj.toString());
                    double tot = qtd * unit;

                    model.setValueAt(tot, i, 3); // atualiza o total da linha
                    soma += tot;

                } catch (NumberFormatException e) {
                    // ignora caso o usuário digite algo inválido

                }
            }
        }

        txtValorProd.setText(String.format("%.2f", soma));
        somarValores(); // soma geral com serviços
    }

    private void somarTabelaServicos() {

        double soma = 0.0;
        DefaultTableModel model = (DefaultTableModel) tblServicosOS.getModel();

        for (int i = 0; i < model.getRowCount(); i++) {

            Object valorObj = model.getValueAt(i, 1);

            if (valorObj != null) {

                try {

                    soma += Double.parseDouble(valorObj.toString());

                } catch (NumberFormatException e) {
                    // ignora valores inválidos
                }
            }
        }

        txtValorServ.setText(String.format("%.2f", soma));
        somarValores(); // 🔄 Atualiza o total geral
    }

    private void somarValores() {

        // Substitui vírgula por ponto e remove espaços para evitar erro de formatação
        String valorServStr = txtValorServ.getText().trim().replace(",", ".");
        String valorProdStr = txtValorProd.getText().trim().replace(",", ".");

        // Inicializa os valores como 0 para evitar erro se o campo estiver vazio
        double valorServico = 0.0;
        double valorProduto = 0.0;

        try {
            // Se o campo de serviço não estiver vazio, converte o texto para número
            if (!valorServStr.isEmpty()) {

                valorServico = Double.parseDouble(valorServStr);
            }

            // Se o campo de produto não estiver vazio, converte o texto para número
            if (!valorProdStr.isEmpty()) {

                valorProduto = Double.parseDouble(valorProdStr);
            }

            // Faz a soma dos dois valores (mesmo que um deles seja 0)
            double total = valorServico + valorProduto;

            // Mostra o resultado formatado com duas casas decimais
            txtOsValor.setText(String.format("%.2f", total));

        } catch (NumberFormatException e) {
            // Caso o usuário digite algo inválido (ex: letras), o campo total zera
            txtOsValor.setText("0.00");

        }
    }

    private void configurarTabelas() {

        // --- Produtos: editar Qtd(1) e Valor Unitário(2) ---
        tblProdutosOS.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Produto", "Qtd", "Valor Unitário", "Total"}
        ) {

            @Override
            public boolean isCellEditable(int row, int col) {

                return col == 1 || col == 2; // só qtd e unit

            }

            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {

                    case 1:
                        return Integer.class;
                    // Qtd
                    case 2:

                    case 3:

                        return Double.class;
                    // Unitário, Total
                    default:

                        return String.class;    // Produto
                }
            }

        });

        // --- Serviços: editar somente Valor(1) ---
        tblServicosOS.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{"Serviço", "Valor"}
        ) {

            @Override
            public boolean isCellEditable(int row, int col) {

                return col == 1; // só valor
            }

            @Override

            public Class<?> getColumnClass(int col) {

                return col == 1 ? Double.class : String.class;
            }
        });

        // Listeners pra recalcular quando editar as células
        addListenersRecalculo();

        // Menus de contexto (botão direito) pra editar/remover
        adicionarMenuContextoProdutos();
        adicionarMenuContextoServicos();
    }

    private void addListenersRecalculo() {

        ((DefaultTableModel) tblProdutosOS.getModel()).addTableModelListener(e -> {

            int col = e.getColumn();

            if (col == 1 || col == 2) {
                // mudou Qtd ou Valor Unitário
                somarTabelaProdutos();
            }
        });

        ((DefaultTableModel) tblServicosOS.getModel()).addTableModelListener(e -> {

            int col = e.getColumn();
            if (col == 1) { // mudou Valor do serviço
                somarTabelaServicos();

            }
        });
    }

    private void adicionarMenuContextoProdutos() {

        JPopupMenu menu = new JPopupMenu();

        JMenuItem editarQtd = new JMenuItem("Editar quantidade");
        editarQtd.addActionListener(a -> {

            int row = tblProdutosOS.getSelectedRow();

            if (row < 0) {

                return;
            }

            tblProdutosOS.editCellAt(row, 1);
            tblProdutosOS.requestFocus();
        });

        JMenuItem editarUnit = new JMenuItem("Editar valor unitário");
        editarUnit.addActionListener(a -> {

            int row = tblProdutosOS.getSelectedRow();
            if (row < 0) {

                return;
            }

            tblProdutosOS.editCellAt(row, 2);
            tblProdutosOS.requestFocus();

        });

        JMenuItem remover = new JMenuItem("Remover produto");
        remover.addActionListener(a -> {

            int row = tblProdutosOS.getSelectedRow();

            if (row < 0) {

                return;
            }

            ((DefaultTableModel) tblProdutosOS.getModel()).removeRow(row);
            somarTabelaProdutos();

        });

        menu.add(editarQtd);
        menu.add(editarUnit);
        menu.addSeparator();
        menu.add(remover);

        tblProdutosOS.setComponentPopupMenu(menu);
    }

    private void adicionarMenuContextoServicos() {

        JPopupMenu menu = new JPopupMenu();

        JMenuItem editarValor = new JMenuItem("Editar valor");

        editarValor.addActionListener(a -> {

            int row = tblServicosOS.getSelectedRow();

            if (row < 0) {

                return;
            }

            tblServicosOS.editCellAt(row, 1);
            tblServicosOS.requestFocus();
        });

        JMenuItem remover = new JMenuItem("Remover serviço");
        remover.addActionListener(a -> {

            int row = tblServicosOS.getSelectedRow();

            if (row < 0) {

                return;
            }

            ((DefaultTableModel) tblServicosOS.getModel()).removeRow(row);
            somarTabelaServicos();
        });

        menu.add(editarValor);
        menu.addSeparator();
        menu.add(remover);

        tblServicosOS.setComponentPopupMenu(menu);
    }

    private void limparCampos() {

        txtData.setText(null);
        txtOs.setText(null);
        txtCliPesquisar.setText(null);
        txtCliIdOs.setText(null);
        txtOsMoto.setText(null);
        txtOsDef.setText(null);
        txtPlaca.setText(null);

        txtOsResp.setText(null);
        txtOsValor.setText(null);
        txtStatusOs.setText(null);
        txtValorServ.setText(null);
        txtValorProd.setText(null);

        cboOsSit.setSelectedItem(" ");

        btnGerarOs.setEnabled(true);
        btnPesquisarOs.setEnabled(true);
        txtCliPesquisar.setEnabled(true);
        tblClientesOs.setVisible(true);

        btnAlterarOs.setEnabled(false);
        btnInativarOs.setEnabled(false);
        btnEmitirOs.setEnabled(false);
        btnAtivar.setEnabled(false);

        ((DefaultTableModel) tblProdutosOS.getModel()).setRowCount(0);
        ((DefaultTableModel) tblServicosOS.getModel()).setRowCount(0);
        ((DefaultTableModel) tblClientesOs.getModel()).setRowCount(0);

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        lblproduto = new javax.swing.JLabel();
        lblmecanico = new javax.swing.JLabel();
        txtValorProd = new javax.swing.JTextField();
        lblvalorProduto = new javax.swing.JLabel();
        lblvalorServico = new javax.swing.JLabel();
        btnPesqMoto = new javax.swing.JLabel();
        btnPesqServ = new javax.swing.JLabel();
        txtOsMoto = new javax.swing.JTextField();
        btnInativarOs = new javax.swing.JButton();
        btnAtivar = new javax.swing.JButton();
        txtPlaca = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        tblProdutosOS = new javax.swing.JTable();
        btnPesqProd = new javax.swing.JLabel();
        lblPlaca = new javax.swing.JLabel();
        btnGerarOs = new javax.swing.JButton();
        lblTotal = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblOs = new javax.swing.JLabel();
        lblData = new javax.swing.JLabel();
        txtOs = new javax.swing.JTextField();
        txtData = new javax.swing.JTextField();
        rbtOrc = new javax.swing.JRadioButton();
        rbtOs = new javax.swing.JRadioButton();
        lblstatusOS = new javax.swing.JLabel();
        txtStatusOs = new javax.swing.JTextField();
        lblSituacao = new javax.swing.JLabel();
        cboOsSit = new javax.swing.JComboBox<>();
        txtOsValor = new javax.swing.JTextField();
        lblMotocicleta = new javax.swing.JLabel();
        txtOsResp = new javax.swing.JTextField();
        pnlCliente = new javax.swing.JPanel();
        txtCliPesquisar = new javax.swing.JTextField();
        lblIdCliente = new javax.swing.JLabel();
        txtCliIdOs = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblClientesOs = new javax.swing.JTable();
        lblLupaCli = new javax.swing.JLabel();
        btnPesquisarOs = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtOsDef = new javax.swing.JTextArea();
        lblServico = new javax.swing.JLabel();
        txtValorServ = new javax.swing.JTextField();
        btnEmitirOs = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tblServicosOS = new javax.swing.JTable();
        btnLimparDados = new javax.swing.JButton();
        btnAlterarOs = new javax.swing.JButton();
        lblDefeito = new javax.swing.JLabel();
        btnPesqMeca = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setTitle("MOTO GESTOR - ORDEM DE SERVIÇO");
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(850, 600));
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameOpened(evt);
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
        });

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        lblproduto.setText("* PRODUTO(S):");

        lblmecanico.setText("MECÂNICO:");

        txtValorProd.setText("0.00");
        txtValorProd.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtValorProdFocusLost(evt);
            }
        });
        txtValorProd.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtValorProdKeyReleased(evt);
            }
        });

        lblvalorProduto.setText("VALOR PRODUTO(S):");

        lblvalorServico.setText("VALOR SERVIÇO(S):");

        btnPesqMoto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqMoto.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqMotoMouseClicked(evt);
            }
        });

        btnPesqServ.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqServ.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqServMouseClicked(evt);
            }
        });

        txtOsMoto.setEditable(false);
        txtOsMoto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOsMotoActionPerformed(evt);
            }
        });

        btnInativarOs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeInativarPadrao_16px.png"))); // NOI18N
        btnInativarOs.setText("Inativar");
        btnInativarOs.setToolTipText("Inativar OS");
        btnInativarOs.setEnabled(false);
        btnInativarOs.setPreferredSize(new java.awt.Dimension(20, 20));
        btnInativarOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInativarOsActionPerformed(evt);
            }
        });

        btnAtivar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAtivarPadrao_16px.png"))); // NOI18N
        btnAtivar.setText("Ativar");
        btnAtivar.setToolTipText("Ativar OS");
        btnAtivar.setEnabled(false);
        btnAtivar.setPreferredSize(new java.awt.Dimension(20, 20));
        btnAtivar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtivarActionPerformed(evt);
            }
        });

        txtPlaca.setEditable(false);
        txtPlaca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtPlacaActionPerformed(evt);
            }
        });

        tblProdutosOS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Produto", "Qtd", "Valor Unitário", "Total"
            }
        ));
        jScrollPane4.setViewportView(tblProdutosOS);

        btnPesqProd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqProd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqProdMouseClicked(evt);
            }
        });

        lblPlaca.setText("* PLACA:");

        btnGerarOs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeAdicionarDocumento_16px.png"))); // NOI18N
        btnGerarOs.setText("Adicionar");
        btnGerarOs.setToolTipText("Gerar OS");
        btnGerarOs.setPreferredSize(new java.awt.Dimension(20, 20));
        btnGerarOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGerarOsActionPerformed(evt);
            }
        });

        lblTotal.setText("VALOR TOTAL:");

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        lblOs.setText("Nº OS:");

        lblData.setText("DATA:");

        txtOs.setEditable(false);

        txtData.setEditable(false);
        txtData.setFont(new java.awt.Font("Tahoma", 1, 9)); // NOI18N

        rbtOrc.setText("Orçamento");
        rbtOrc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtOrcActionPerformed(evt);
            }
        });

        rbtOs.setText("Ordem de Serviço");
        rbtOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rbtOsActionPerformed(evt);
            }
        });

        lblstatusOS.setText("STATUS OS:");

        txtStatusOs.setEditable(false);
        txtStatusOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStatusOsActionPerformed(evt);
            }
        });

        lblSituacao.setText("* SITUAÇÃO:");

        cboOsSit.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { " ", "Aguardando aprovação", "Orçamento Aprovado", "Orçamento REPROVADO", "Aguardando peças", "Em Manutenção", "OS Concluída", "Retornou (Garantia)" }));
        cboOsSit.setToolTipText("");
        cboOsSit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cboOsSitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblOs)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtOs, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(rbtOrc)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(lblstatusOS)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtStatusOs, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(rbtOs, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addGap(37, 37, 37))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblSituacao)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboOsSit, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 52, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOs)
                    .addComponent(txtOs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblstatusOS)
                    .addComponent(txtStatusOs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblData))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbtOrc)
                    .addComponent(rbtOs))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSituacao)
                    .addComponent(cboOsSit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        txtOsValor.setText("0.00");
        txtOsValor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOsValorActionPerformed(evt);
            }
        });

        lblMotocicleta.setText("* MOTOCICLETA:");

        txtOsResp.setEditable(false);
        txtOsResp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtOsRespActionPerformed(evt);
            }
        });

        pnlCliente.setBorder(javax.swing.BorderFactory.createTitledBorder("Cliente"));
        pnlCliente.setPreferredSize(new java.awt.Dimension(335, 143));
        pnlCliente.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                pnlClienteMouseClicked(evt);
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

        lblIdCliente.setText("* ID");

        txtCliIdOs.setEditable(false);

        tblClientesOs = new javax.swing.JTable(){
            public  boolean isCellEditable(int rowIndex, int colIndex){
                return false;
            }
        };
        tblClientesOs.setModel(new javax.swing.table.DefaultTableModel(
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
        tblClientesOs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblClientesOsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblClientesOs);

        lblLupaCli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        lblLupaCli.setText("* CAMPOS OBRIGATÓRIOS");
        lblLupaCli.setPreferredSize(new java.awt.Dimension(25, 25));

        javax.swing.GroupLayout pnlClienteLayout = new javax.swing.GroupLayout(pnlCliente);
        pnlCliente.setLayout(pnlClienteLayout);
        pnlClienteLayout.setHorizontalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlClienteLayout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(pnlClienteLayout.createSequentialGroup()
                        .addComponent(txtCliPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, 183, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblLupaCli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblIdCliente)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtCliIdOs, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 19, 19))))
        );
        pnlClienteLayout.setVerticalGroup(
            pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCliIdOs, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblIdCliente))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
            .addGroup(pnlClienteLayout.createSequentialGroup()
                .addGroup(pnlClienteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCliPesquisar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLupaCli, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        btnPesquisarOs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconePesq_16px.png"))); // NOI18N
        btnPesquisarOs.setText("Pesquisar");
        btnPesquisarOs.setToolTipText("Consultar OS");
        btnPesquisarOs.setPreferredSize(new java.awt.Dimension(20, 20));
        btnPesquisarOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPesquisarOsActionPerformed(evt);
            }
        });

        txtOsDef.setColumns(20);
        txtOsDef.setRows(5);
        jScrollPane2.setViewportView(txtOsDef);

        lblServico.setText("* SERVIÇO:");

        txtValorServ.setText("0.00");
        txtValorServ.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtValorServFocusLost(evt);
            }
        });
        txtValorServ.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtValorServActionPerformed(evt);
            }
        });
        txtValorServ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtValorServKeyReleased(evt);
            }
        });

        btnEmitirOs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeImprimirOs_16px.png"))); // NOI18N
        btnEmitirOs.setText("Imprimir");
        btnEmitirOs.setToolTipText("Emitir OS");
        btnEmitirOs.setEnabled(false);
        btnEmitirOs.setPreferredSize(new java.awt.Dimension(20, 20));
        btnEmitirOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEmitirOsActionPerformed(evt);
            }
        });

        tblServicosOS.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {},
            new String [] {
                "Serviço", "Valor"
            }
        ));
        jScrollPane6.setViewportView(tblServicosOS);

        btnLimparDados.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        btnLimparDados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/clean.png"))); // NOI18N
        btnLimparDados.setText("LimparCampos");
        btnLimparDados.setToolTipText("Limpar Dados");
        btnLimparDados.setPreferredSize(new java.awt.Dimension(20, 20));
        btnLimparDados.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparDadosActionPerformed(evt);
            }
        });

        btnAlterarOs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/IconeEditarDocumento_16px.png"))); // NOI18N
        btnAlterarOs.setText("Editar");
        btnAlterarOs.setToolTipText("Alterar Dados OS");
        btnAlterarOs.setEnabled(false);
        btnAlterarOs.setPreferredSize(new java.awt.Dimension(20, 20));
        btnAlterarOs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlterarOsActionPerformed(evt);
            }
        });

        lblDefeito.setText("* DEFEITO:");

        btnPesqMeca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/br/com/motogestor/icones/search (pesquisa).png"))); // NOI18N
        btnPesqMeca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnPesqMecaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(114, 114, 114)
                                .addComponent(txtOsMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnPesqMoto, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(37, 37, 37)
                                .addComponent(lblPlaca)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(52, 52, 52)
                                .addComponent(lblServico)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnPesqServ)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(18, 18, 18)
                                        .addComponent(lblDefeito))
                                    .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jScrollPane2))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(pnlCliente, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(15, 15, 15)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(lblvalorServico)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtValorServ, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(btnGerarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(31, 31, 31)
                                            .addComponent(btnAlterarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGap(31, 31, 31)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lblvalorProduto)
                                        .addComponent(btnPesquisarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(13, 13, 13)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtValorProd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnInativarOs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(39, 39, 39)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(40, 40, 40)
                                            .addComponent(btnEmitirOs, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addComponent(lblTotal)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(txtOsValor, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(28, 28, 28)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                            .addGap(194, 194, 194)
                                            .addComponent(lblproduto)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(btnPesqProd))
                                        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(8, 8, 8))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(lblmecanico)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(txtOsResp, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(btnPesqMeca)))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(lblMotocicleta)))
                        .addGap(0, 176, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlCliente, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblMotocicleta)
                            .addComponent(txtOsMoto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnPesqMoto, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnPesqMeca, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblPlaca)
                                .addComponent(txtPlaca, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblmecanico)
                                .addComponent(txtOsResp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(14, 14, 14)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(lblDefeito)
                        .addGap(32, 32, 32)
                        .addComponent(btnLimparDados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnPesqServ, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesqProd)
                    .addComponent(lblproduto)
                    .addComponent(lblServico))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtValorProd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblvalorServico)
                        .addComponent(txtValorServ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTotal)
                        .addComponent(txtOsValor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblvalorProduto)))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnGerarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAlterarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPesquisarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAtivar, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEmitirOs, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnInativarOs, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        jScrollPane3.setViewportView(jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
        );

        setBounds(0, 0, 850, 600);
    }// </editor-fold>//GEN-END:initComponents

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {//GEN-FIRST:event_formInternalFrameOpened
        //ao abrir o form marcar uma das opções do RadioButton

        //selecionando a variável do RadioButton "Orçamento".
        rbtOrc.setSelected(true);

        //Atribuindo a variável tipo o texto "Orçamento", vinculo com o banco de dados.
        tipo = "Orçamento";
    }//GEN-LAST:event_formInternalFrameOpened

    private void txtValorProdFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtValorProdFocusLost
        somarValores();
    }//GEN-LAST:event_txtValorProdFocusLost

    private void txtValorProdKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtValorProdKeyReleased
        somarValores();
    }//GEN-LAST:event_txtValorProdKeyReleased

    private void btnPesqMotoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqMotoMouseClicked
        pesquisarMoto();
        
        txtCliPesquisar.setEnabled(false);
        
    }//GEN-LAST:event_btnPesqMotoMouseClicked

    private void btnPesqServMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqServMouseClicked
        // TODO add your handling code here:
        pesquisarServico();
    }//GEN-LAST:event_btnPesqServMouseClicked

    private void txtOsMotoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOsMotoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOsMotoActionPerformed

    private void btnInativarOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInativarOsActionPerformed
        // chamando o método InativarOs();

        inativarOs();
    }//GEN-LAST:event_btnInativarOsActionPerformed

    private void btnAtivarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAtivarActionPerformed
        ativarOS();
    }//GEN-LAST:event_btnAtivarActionPerformed

    private void txtPlacaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtPlacaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtPlacaActionPerformed

    private void btnPesqProdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqProdMouseClicked
        pesquisarProduto();
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPesqProdMouseClicked

    private void btnGerarOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGerarOsActionPerformed
        // chamando o método emitir_os();

        emitirOS();
    }//GEN-LAST:event_btnGerarOsActionPerformed

    private void rbtOrcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtOrcActionPerformed
        // atribuindo um texto a variável "tipo" se selecionado.

        tipo = "Orçamento";
    }//GEN-LAST:event_rbtOrcActionPerformed

    private void rbtOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rbtOsActionPerformed
        // a linha rabaixo atribui um texto a variável tipo se o RadioButton estiver selecionado

        tipo = "OS";
    }//GEN-LAST:event_rbtOsActionPerformed

    private void txtStatusOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStatusOsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStatusOsActionPerformed

    private void cboOsSitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cboOsSitActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cboOsSitActionPerformed

    private void txtOsValorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOsValorActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOsValorActionPerformed

    private void txtOsRespActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtOsRespActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtOsRespActionPerformed

    private void txtCliPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCliPesquisarActionPerformed

    }//GEN-LAST:event_txtCliPesquisarActionPerformed

    private void txtCliPesquisarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtCliPesquisarKeyReleased
        // CHAMANDO O MÉTODO PESQUISAR CLIENTE

        pesquisarCliente();
    }//GEN-LAST:event_txtCliPesquisarKeyReleased

    private void tblClientesOsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblClientesOsMouseClicked
        // chamando o método preencher_campos_os();

        preencherCamposOs();
    }//GEN-LAST:event_tblClientesOsMouseClicked

    private void btnPesquisarOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPesquisarOsActionPerformed
        // chamando o método pesqusiar_os;

        pesquisarOS();
    }//GEN-LAST:event_btnPesquisarOsActionPerformed

    private void txtValorServFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtValorServFocusLost
        somarValores();
    }//GEN-LAST:event_txtValorServFocusLost

    private void txtValorServActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtValorServActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtValorServActionPerformed

    private void txtValorServKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtValorServKeyReleased
        somarValores();
    }//GEN-LAST:event_txtValorServKeyReleased

    private void btnEmitirOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmitirOsActionPerformed
        //CHAMANDO O MÉTODO imprimir_os()

        imprimirOS();
    }//GEN-LAST:event_btnEmitirOsActionPerformed

    private void btnLimparDadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparDadosActionPerformed
        limparCampos();
    }//GEN-LAST:event_btnLimparDadosActionPerformed

    private void btnAlterarOsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlterarOsActionPerformed
        // chamando o método alterar_os();

        alterarOS();
    }//GEN-LAST:event_btnAlterarOsActionPerformed

    private void btnPesqMecaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnPesqMecaMouseClicked
       pesquisarMecanico();
    }//GEN-LAST:event_btnPesqMecaMouseClicked

    private void pnlClienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlClienteMouseClicked
    limparCampos();
   
    tblClientesOs.clearSelection();
    }//GEN-LAST:event_pnlClienteMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAlterarOs;
    private javax.swing.JButton btnAtivar;
    private javax.swing.JButton btnEmitirOs;
    private javax.swing.JButton btnGerarOs;
    private javax.swing.JButton btnInativarOs;
    private javax.swing.JButton btnLimparDados;
    private javax.swing.JLabel btnPesqMeca;
    private javax.swing.JLabel btnPesqMoto;
    private javax.swing.JLabel btnPesqProd;
    private javax.swing.JLabel btnPesqServ;
    private javax.swing.JButton btnPesquisarOs;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboOsSit;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JLabel lblData;
    private javax.swing.JLabel lblDefeito;
    private javax.swing.JLabel lblIdCliente;
    private javax.swing.JLabel lblLupaCli;
    private javax.swing.JLabel lblMotocicleta;
    private javax.swing.JLabel lblOs;
    private javax.swing.JLabel lblPlaca;
    private javax.swing.JLabel lblServico;
    private javax.swing.JLabel lblSituacao;
    private javax.swing.JLabel lblTotal;
    private javax.swing.JLabel lblmecanico;
    private javax.swing.JLabel lblproduto;
    private javax.swing.JLabel lblstatusOS;
    private javax.swing.JLabel lblvalorProduto;
    private javax.swing.JLabel lblvalorServico;
    private javax.swing.JPanel pnlCliente;
    private javax.swing.JRadioButton rbtOrc;
    private javax.swing.JRadioButton rbtOs;
    private javax.swing.JTable tblClientesOs;
    private javax.swing.JTable tblProdutosOS;
    private javax.swing.JTable tblServicosOS;
    private javax.swing.JTextField txtCliIdOs;
    private javax.swing.JTextField txtCliPesquisar;
    private javax.swing.JTextField txtData;
    private javax.swing.JTextField txtOs;
    private javax.swing.JTextArea txtOsDef;
    private javax.swing.JTextField txtOsMoto;
    private javax.swing.JTextField txtOsResp;
    private javax.swing.JTextField txtOsValor;
    private javax.swing.JTextField txtPlaca;
    private javax.swing.JTextField txtStatusOs;
    private javax.swing.JTextField txtValorProd;
    private javax.swing.JTextField txtValorServ;
    // End of variables declaration//GEN-END:variables
}

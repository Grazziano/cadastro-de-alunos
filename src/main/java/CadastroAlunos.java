
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CadastroAlunos extends JFrame {

    private JTextField txtNome = new JTextField(20);
    private JTextField txtEmail = new JTextField(20);
    private JTextField txtCurso = new JTextField(20);

    private JTable tabela;
    private DefaultTableModel modelo;
    private int idSelecionado = -1;

    public CadastroAlunos() {
        setTitle("Sistema de Cadastro de Alunos");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Painel do formulário
        JPanel formulario = new JPanel(new GridLayout(3, 2, 5, 5));
        formulario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formulario.add(new JLabel("Nome:"));
        formulario.add(txtNome);

        formulario.add(new JLabel("E-mail:"));
        formulario.add(txtEmail);

        formulario.add(new JLabel("Curso:"));
        formulario.add(txtCurso);

        // Botões
        JButton btnSalvar = new JButton("Salvar");
        JButton btnEditar = new JButton("Editar");
        JButton btnExcluir = new JButton("Excluir");
        JButton btnLimpar = new JButton("Limpar");

        JPanel botoes = new JPanel();

        botoes.add(btnSalvar);
        botoes.add(btnEditar);
        botoes.add(btnExcluir);
        botoes.add(btnLimpar);

        // Tabela
        modelo = new DefaultTableModel(
                new String[]{"ID", "Nome", "E-mail", "Curso"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        JScrollPane rolagem = new JScrollPane(tabela);

        add(formulario, BorderLayout.NORTH);
        add(rolagem, BorderLayout.CENTER);
        add(botoes, BorderLayout.SOUTH);

        // Eventos dos botões
        btnSalvar.addActionListener(e -> salvar());
        btnEditar.addActionListener(e -> editar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e -> limpar());

        // Selecionar aluno na tabela
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && tabela.getSelectedRow() != -1) {

                int linha = tabela.getSelectedRow();

                idSelecionado = Integer.parseInt(
                        modelo.getValueAt(linha, 0).toString()
                );

                txtNome.setText(
                        modelo.getValueAt(linha, 1).toString()
                );

                txtEmail.setText(
                        modelo.getValueAt(linha, 2).toString()
                );

                txtCurso.setText(
                        modelo.getValueAt(linha, 3).toString()
                );
            }
        });

        carregarAlunos();
    }

    // CREATE: cadastrar aluno
    private void salvar() {
        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Informe o nome do aluno."
            );
            return;
        }

        String sql =
                "INSERT INTO alunos (nome, email, curso) VALUES (?, ?, ?)";

        try (Connection con = Conexao.conectar();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setString(2, txtEmail.getText().trim());
            stmt.setString(3, txtCurso.getText().trim());

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Aluno cadastrado!");
            limpar();
            carregarAlunos();

        } catch (SQLException ex) {
            erro(ex);
        }
    }

    // READ: listar alunos
    private void carregarAlunos() {
        modelo.setRowCount(0);

        String sql =
                "SELECT id, nome, email, curso FROM alunos ORDER BY id";

        try (Connection con = Conexao.conectar();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("curso")
                });
            }

        } catch (SQLException ex) {
            erro(ex);
        }
    }

    // UPDATE: editar aluno selecionado
    private void editar() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(
                    this, "Selecione um aluno na tabela."
            );
            return;
        }

        if (txtNome.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Informe o nome do aluno."
            );
            return;
        }

        String sql =
                "UPDATE alunos SET nome=?, email=?, curso=? WHERE id=?";

        try (Connection con = Conexao.conectar();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, txtNome.getText().trim());
            stmt.setString(2, txtEmail.getText().trim());
            stmt.setString(3, txtCurso.getText().trim());
            stmt.setInt(4, idSelecionado);

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Aluno atualizado!");
            limpar();
            carregarAlunos();

        } catch (SQLException ex) {
            erro(ex);
        }
    }

    // DELETE: excluir aluno selecionado
    private void excluir() {
        if (idSelecionado == -1) {
            JOptionPane.showMessageDialog(
                    this, "Selecione um aluno na tabela."
            );
            return;
        }

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente excluir este aluno?",
                "Confirmar exclusão",
                JOptionPane.YES_NO_OPTION
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM alunos WHERE id=?";

        try (Connection con = Conexao.conectar();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, idSelecionado);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Aluno excluído!");
            limpar();
            carregarAlunos();

        } catch (SQLException ex) {
            erro(ex);
        }
    }

    // Limpar os campos
    private void limpar() {
        txtNome.setText("");
        txtEmail.setText("");
        txtCurso.setText("");
        idSelecionado = -1;
        tabela.clearSelection();
    }

    // Exibir erros de conexão ou SQL
    private void erro(SQLException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Erro ao acessar o banco de dados:\n" + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CadastroAlunos().setVisible(true);
        });
    }
}

O que falta para a Versão 1.0 ficar realmente completa

Para considerar a 1.0 backend “fechada”, eu vejo que faltam estes blocos:

validação robusta de requests
padronização de respostas e erros
busca textual de produtos e clientes
consistência de CPF/e-mail/telefone e duplicidade
detalhamento e filtros de vendas
dashboard expandido com métricas por período
histórico consultável de movimentações de estoque
testes automatizados essenciais
algum mecanismo mínimo de backup/exportação do banco ou rotina orientada para isso.
Etapa 2 — Lista priorizada do que implementar primeiro

Eu seguiria nesta ordem:

Prioridade 1 — Base estrutural

DTOs de request/response para produto, cliente e venda
validações com Bean Validation
exceções customizadas
@ControllerAdvice com resposta padronizada de erro

Isso vem primeiro porque melhora todos os módulos ao mesmo tempo.

Prioridade 2 — Produtos

validações de criação/edição
busca textual
proteção para estoque e preços inválidos
endpoint de histórico de movimentações por produto

Produtos impactam vendas e dashboard, então precisam ficar firmes antes.

Prioridade 3 — Clientes

validações de nome, CPF, e-mail, telefone
prevenção de CPF duplicado
busca textual por nome/cpf/telefone
inativação segura

Clientes são simples, mas precisam ficar confiáveis antes de fechar vendas.

Prioridade 4 — Vendas

detalhamento por id
listagem com filtros por período/status/cliente
validações completas de carrinho, desconto, produto ativo, quantidade
respostas mais adequadas para histórico
cancelamento com regras mais claras

Esse é o núcleo mais sensível da regra de negócio.

Prioridade 5 — Dashboard

vendas do dia
vendas da semana
vendas do mês
faturamento por período
contagem separando vendas concluídas/canceladas
talvez ticket médio

Isso fecha melhor a 1.0.

Prioridade 6 — Qualidade

testes unitários/integrados
README atualizado
preparação para login e multi-loja com campos/estrutura extensível
base para backup
Etapa 3 — Ordem recomendada de implementação

A melhor ordem para mexer agora é:

melhorias estruturais
produtos
clientes
vendas
dashboard
testes e acabamento

Eu começaria pelas melhorias estruturais porque elas evitam retrabalho nos módulos seguintes.

Resumo direto

Hoje seu backend está em um estado de MVP funcional, não de backend 1.0 fechado.
A boa notícia é que tem bastante coisa pronta e aproveitável, então não faz sentido recomeçar.
O que falta agora é endurecer regras, padronizar API e completar consultas e indicadores.

A próxima etapa ideal é começar por melhorias estruturais:

DTOs próprios
validações
exceptions customizadas
handler global de erros
padrão de resposta

Depois disso a gente entra em produtos já do jeito certo.
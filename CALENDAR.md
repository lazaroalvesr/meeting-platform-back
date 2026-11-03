# Calendário

O calendário salva anotações por dia no PostgreSQL, separadas por usuário autenticado. Permite criar, editar e excluir anotações. Não envia e-mails ou notificações.

A página inicial mostra um mini calendário ao lado do gráfico de faturamento. Os dias com anotações são marcados; clicar em um dia abre a agenda completa nessa data.

A migration V25 é mantida para compatibilidade com bancos existentes. As antigas colunas de lembretes permanecem inativas; nenhum agendador ou serviço de envio de e-mail é executado.

UPDATE payments AS payment
SET due_date = project.delivery_date
FROM projects AS project
WHERE payment.project_id = project.id
  AND payment.status IN ('PENDING', 'OVERDUE')
  AND project.delivery_date IS NOT NULL
  AND (
    payment.description LIKE 'Entrada 50% - %'
    OR payment.description LIKE 'Saldo final 50% - %'
    OR payment.description LIKE 'Pagamento do projeto - %'
  );

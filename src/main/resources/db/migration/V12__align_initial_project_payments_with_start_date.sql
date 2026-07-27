UPDATE payments AS payment
SET due_date = project.start_date
FROM projects AS project
WHERE payment.project_id = project.id
  AND payment.status IN ('PENDING', 'OVERDUE')
  AND project.start_date IS NOT NULL
  AND (
    payment.description LIKE 'Entrada 50% - %'
    OR payment.description LIKE 'Pagamento do projeto - %'
  );

UPDATE payments AS payment
SET due_date = project.delivery_date
FROM projects AS project
WHERE payment.project_id = project.id
  AND payment.description LIKE 'Saldo final 50% - %'
  AND payment.status IN ('PENDING', 'OVERDUE')
  AND project.delivery_date IS NOT NULL;

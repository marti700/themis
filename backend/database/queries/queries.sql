-- name: GetCustomer :one
SELECT * FROM customers
WHERE id = $1 LIMIT 1;

-- name: CreateCustomer :one
INSERT INTO customers (
    first_name, last_name, birthday, address, marital_status, occupation, id_number
) VALUES (
    $1, $2, $3, $4, $5, $6, $7
)
RETURNING *;

-- name: GetDocumentsByCustomer :many
SELECT d.* FROM documents d
JOIN customer_documents cd ON d.id = cd.document_id
WHERE cd.customer_id = $1;

-- name: LinkDocumentToCustomer :exec
INSERT INTO customer_documents (customer_id, document_id)
VALUES ($1, $2);

-- name: GetCustomersByCompany :many
SELECT c.* FROM customers c
JOIN company_customers cc ON c.id = cc.customer_id
WHERE cc.company_id = $1;

-- name: GetCompaniesByCustomer :many
SELECT co.* FROM companies co
JOIN company_customers cc ON co.id = cc.company_id
WHERE cc.customer_id = $1;

-- name: AddCustomerToCompany :exec
INSERT INTO company_customers (company_id, customer_id)
VALUES ($1, $2)
ON CONFLICT DO NOTHING;
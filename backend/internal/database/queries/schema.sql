CREATE TYPE marital_status AS ENUM (
    'single', 
    'married', 
    'divorced', 
    'widowed', 
    'legal_union'
);

CREATE TABLE companies (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) UNIQUE NOT NULL,
    industry VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE customers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birthday DATE,
    address TEXT,
    marital_status marital_status,
    occupation VARCHAR(255),
    id_number VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Join Table: Customers <-> Companies (Many-to-Many)
CREATE TABLE company_customers (
    company_id INTEGER REFERENCES companies(id) ON DELETE CASCADE,
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (company_id, customer_id)
);

CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    file_path TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Join Table: Customers <-> Documents (Many-to-Many)
CREATE TABLE customer_documents (
    customer_id INTEGER REFERENCES customers(id) ON DELETE CASCADE,
    document_id INTEGER REFERENCES documents(id) ON DELETE CASCADE,
    PRIMARY KEY (customer_id, document_id)
);

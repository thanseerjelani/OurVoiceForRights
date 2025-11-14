-- Sample data for testing (will be replaced by API sync)

-- Insert States
INSERT INTO states (name, state_code) VALUES
('Bihar', 'BR'),
('Uttar Pradesh', 'UP'),
('West Bengal', 'WB'),
('Maharashtra', 'MH'),
('Tamil Nadu', 'TN')
ON CONFLICT DO NOTHING;

-- Insert Districts for Bihar
INSERT INTO districts (name, district_code, state_id)
SELECT 'Patna', 'BR-PT', id FROM states WHERE name = 'Bihar'
UNION ALL
SELECT 'Gaya', 'BR-GY', id FROM states WHERE name = 'Bihar'
UNION ALL
SELECT 'Muzaffarpur', 'BR-MZ', id FROM states WHERE name = 'Bihar'
ON CONFLICT DO NOTHING;

-- Insert Districts for Uttar Pradesh
INSERT INTO districts (name, district_code, state_id)
SELECT 'Lucknow', 'UP-LC', id FROM states WHERE name = 'Uttar Pradesh'
UNION ALL
SELECT 'Kanpur', 'UP-KN', id FROM states WHERE name = 'Uttar Pradesh'
ON CONFLICT DO NOTHING;

-- Insert Sample Performance Data for Patna
INSERT INTO performance (
    district_id, month_name, fin_year, total_households_worked, average_days_employment,
    total_wages, ongoing_works, completed_works, total_expenditure, avg_wage_rate, timestamp, data_source
)
SELECT
    d.id, 'Sep', '2024-2025', 45000, 85.5, 12500000.00, 125, 98, 15000000.00, 280.50, NOW(), 'sample_data'
FROM districts d
JOIN states s ON d.state_id = s.id
WHERE d.name = 'Patna' AND s.name = 'Bihar'
ON CONFLICT DO NOTHING;

INSERT INTO performance (
    district_id, month_name, fin_year, total_households_worked, average_days_employment,
    total_wages, ongoing_works, completed_works, total_expenditure, avg_wage_rate, timestamp, data_source
)
SELECT
    d.id, 'Aug', '2024-2025', 43000, 82.0, 11800000.00, 120, 95, 14000000.00, 275.00, NOW(), 'sample_data'
FROM districts d
JOIN states s ON d.state_id = s.id
WHERE d.name = 'Patna' AND s.name = 'Bihar'
ON CONFLICT DO NOTHING;
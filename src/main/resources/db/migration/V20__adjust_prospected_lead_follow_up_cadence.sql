UPDATE prospected_leads
SET next_follow_up_date = CASE follow_up_count
    WHEN 0 THEN ((prospected_at AT TIME ZONE 'America/Sao_Paulo')::date + 7)
    WHEN 1 THEN ((prospected_at AT TIME ZONE 'America/Sao_Paulo')::date + 15)
    WHEN 2 THEN ((prospected_at AT TIME ZONE 'America/Sao_Paulo')::date + 30)
    ELSE COALESCE(
        (((last_follow_up_at AT TIME ZONE 'America/Sao_Paulo')::date + INTERVAL '1 month')::date),
        ((prospected_at AT TIME ZONE 'America/Sao_Paulo')::date + 60)
    )
END;

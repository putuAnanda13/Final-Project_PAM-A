-- App profile rows linked to Supabase Auth users.
-- Run through Supabase CLI or the Supabase SQL Editor.

create table if not exists public.profile (
    id uuid primary key references auth.users (id) on delete cascade,
    nama text not null,
    email text not null,
    jenis_kelamin text,
    tanggal_lahir date,
    tinggi_badan numeric,
    berat_badan numeric,
    target_kalori integer not null default 2000,
    aktivitas_harian jsonb not null default '[]'::jsonb
);

alter table public.profile enable row level security;

drop policy if exists "Users can view own profile" on public.profile;
create policy "Users can view own profile"
    on public.profile for select to authenticated
    using (auth.uid() = id);

drop policy if exists "Users can update own profile" on public.profile;
create policy "Users can update own profile"
    on public.profile for update to authenticated
    using (auth.uid() = id)
    with check (auth.uid() = id);

create or replace function public.create_profile_for_auth_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profile (id, nama, email)
    values (
        new.id,
        coalesce(
            nullif(new.raw_user_meta_data ->> 'full_name', ''),
            nullif(new.raw_user_meta_data ->> 'name', ''),
            nullif(split_part(coalesce(new.email, ''), '@', 1), ''),
            'Pengguna'
        ),
        coalesce(new.email, '')
    )
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created_profile on auth.users;
create trigger on_auth_user_created_profile
    after insert on auth.users
    for each row execute function public.create_profile_for_auth_user();

-- Existing accounts are backfilled once; future accounts are handled by trigger.
insert into public.profile (id, nama, email)
select
    u.id,
    coalesce(nullif(u.raw_user_meta_data ->> 'full_name', ''), nullif(u.raw_user_meta_data ->> 'name', ''), nullif(split_part(coalesce(u.email, ''), '@', 1), ''), 'Pengguna'),
    coalesce(u.email, '')
from auth.users u
on conflict (id) do nothing;

grant select, update on public.profile to authenticated;

-- Upgrade an installation that already ran the earlier plural `profiles`
-- migration. Fresh installations already have the target schema from 001.
do $$
begin
    if to_regclass('public.profile') is null
       and to_regclass('public.profiles') is not null then
        alter table public.profiles rename to profile;
    end if;

    if to_regclass('public.profile') is not null then
        if exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='full_name')
           and not exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='nama') then
            alter table public.profile rename column full_name to nama;
        end if;
        if exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='gender')
           and not exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='jenis_kelamin') then
            alter table public.profile rename column gender to jenis_kelamin;
        end if;
        if exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='height_cm')
           and not exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='tinggi_badan') then
            alter table public.profile rename column height_cm to tinggi_badan;
        end if;
        if exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='weight_kg')
           and not exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='berat_badan') then
            alter table public.profile rename column weight_kg to berat_badan;
        end if;
        if exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='daily_calorie_target')
           and not exists (select 1 from information_schema.columns where table_schema='public' and table_name='profile' and column_name='target_kalori') then
            alter table public.profile rename column daily_calorie_target to target_kalori;
        end if;
    end if;
end;
$$;

create table if not exists public.profile (
    id uuid primary key references auth.users (id) on delete cascade,
    nama text not null default '',
    email text not null default '',
    jenis_kelamin text,
    tanggal_lahir date,
    tinggi_badan numeric,
    berat_badan numeric,
    target_kalori integer not null default 2000,
    aktivitas_harian jsonb not null default '[]'::jsonb
);

alter table public.profile add column if not exists nama text not null default '';
alter table public.profile add column if not exists email text not null default '';
alter table public.profile add column if not exists jenis_kelamin text;
alter table public.profile add column if not exists tanggal_lahir date;
alter table public.profile add column if not exists tinggi_badan numeric;
alter table public.profile add column if not exists berat_badan numeric;
alter table public.profile add column if not exists target_kalori integer not null default 2000;
alter table public.profile add column if not exists aktivitas_harian jsonb not null default '[]'::jsonb;

-- `id` must be the authenticated user's UUID, not a separately generated UUID.
alter table public.profile alter column id drop default;

update public.profile p
   set email = coalesce(nullif(p.email, ''), u.email, ''),
       nama = coalesce(nullif(p.nama, ''), nullif(u.raw_user_meta_data ->> 'full_name', ''), nullif(u.raw_user_meta_data ->> 'name', ''), split_part(coalesce(u.email, ''), '@', 1), 'Pengguna')
  from auth.users u
 where p.id = u.id
   and (p.email = '' or p.nama = '');

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
        coalesce(nullif(new.raw_user_meta_data ->> 'full_name', ''), nullif(new.raw_user_meta_data ->> 'name', ''), nullif(split_part(coalesce(new.email, ''), '@', 1), ''), 'Pengguna'),
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

insert into public.profile (id, nama, email)
select
    u.id,
    coalesce(nullif(u.raw_user_meta_data ->> 'full_name', ''), nullif(u.raw_user_meta_data ->> 'name', ''), nullif(split_part(coalesce(u.email, ''), '@', 1), ''), 'Pengguna'),
    coalesce(u.email, '')
from auth.users u
on conflict (id) do nothing;

grant select, update on public.profile to authenticated;

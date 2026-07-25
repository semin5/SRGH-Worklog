import { useEffect, useMemo, useState } from 'react'
import './quick-schedule.css'
import './catalog.css'
import './scheduler.css'

const today = () => new Date().toISOString().slice(0, 10)

export function ScheduleQuickForm() {
  const [items, setItems] = useState([])
  const load = async () => { const response = await fetch('/api/schedules/admin'); if (response.ok) setItems(await response.json()) }
  useEffect(() => { load() }, [])
  const groups = useMemo(() => {
    const currentMonth = new Date(); currentMonth.setDate(1); currentMonth.setHours(0, 0, 0, 0)
    const occurrences = items.map(item => {
      const start = new Date(`${item.start_date}T00:00:00`), step = Number(item.period_months || 0)
      if (!step) return start < currentMonth ? null : { ...item, date: item.start_date }
      let date = new Date(start)
      while (date < currentMonth) date = new Date(date.getFullYear(), date.getMonth() + step, Math.min(start.getDate(), new Date(date.getFullYear(), date.getMonth() + step + 1, 0).getDate()))
      return { ...item, date: date.toISOString().slice(0, 10) }
    }).filter(Boolean).sort((a, b) => a.date.localeCompare(b.date))
    return occurrences.reduce((result, item) => { const month = item.date.slice(0, 7); (result[month] ||= []).push(item); return result }, {})
  }, [items])
  const complete = async item => {
    if (item.completed) return
    setItems(previous => previous.map(entry => entry.id === item.id ? { ...entry, completed: true, completed_date: today() } : entry))
    const response = await fetch(`/api/schedules/${item.id}/complete`, { method: 'PATCH' })
    if (response.ok) load(); else { alert(`완료 상태 저장에 실패했습니다. (${response.status})`); load() }
  }
  const currentMonth = today().slice(0, 7)
  const currentEntries = groups[currentMonth] || []
  const previewGroups = Object.entries(groups).filter(([month]) => month !== currentMonth)
  const previewPendingCount = previewGroups.reduce((total, [, entries]) => total + entries.filter(item => !item.completed).length, 0)
  const tasks = entries => <div className="schedule-task-list">{entries.map(item => <button key={`${item.id}-${item.date}`} className={item.completed ? 'completed' : ''} onClick={event => { event.preventDefault(); complete(item) }}><span>{item.title}</span></button>)}</div>
  return <div className="quick-schedule"><h3>월간 스케줄러</h3><details open className="schedule-month"><summary>{currentMonth.replace('-', '.')}<span>{currentEntries.filter(item => !item.completed).length}</span></summary>{currentEntries.length ? tasks(currentEntries) : <p className="no-schedules">이번 달 스케줄이 없습니다.</p>}</details>{previewGroups.length > 0 && <details className="schedule-preview"><summary>미리보기 <span>{previewPendingCount}</span></summary><div className="schedule-preview-list">{previewGroups.map(([month, entries]) => <details className="schedule-preview-month" key={month}><summary>{month.replace('-', '.')}<span>{entries.filter(item => !item.completed).length}</span></summary>{tasks(entries)}</details>)}</div></details>}{Object.keys(groups).length === 0 && <p className="no-schedules">등록된 스케줄이 없습니다.</p>}</div>
}

function CatalogAccordion({ type, label }) {
  const [open, setOpen] = useState(false), [items, setItems] = useState([]), [name, setName] = useState(''), [editing, setEditing] = useState(null)
  const load = async () => { const response = await fetch(`/api/admin/catalogs/${type}`); if (response.ok) setItems(await response.json()) }
  useEffect(() => { if (open) load() }, [open])
  const save = async event => { event.preventDefault(); const response = await fetch(editing ? `/api/admin/catalogs/${type}/${editing}` : `/api/admin/catalogs/${type}`, { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ name }) }); if (!response.ok) return alert(`저장에 실패했습니다. (${response.status})`); setEditing(null); setName(''); load() }
  const remove = async id => { if (!confirm('삭제할까요?')) return; const response = await fetch(`/api/admin/catalogs/${type}/${id}`, { method: 'DELETE' }); if (response.ok) load() }
  const setActive = async item => { const response = await fetch(`/api/admin/catalogs/processors/${item.id}/active`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ active: !item.active }) }); if (response.ok) load(); else alert('활성 상태 변경에 실패했습니다.') }
  return <details className="admin-accordion" open={open} onToggle={event => setOpen(event.currentTarget.open)}><summary>{label}<span>{open ? '⌃' : '⌄'}</span></summary><div className="accordion-content"><div className="accordion-list">{items.length === 0 && <p>저장된 항목이 없습니다.</p>}{items.map(item => <article key={item.id}><span>{item.name}</span><div>{type === 'processors' && <button className={item.active ? 'active-toggle' : ''} onClick={() => setActive(item)}>{item.active ? '활성' : '비활성'}</button>}<button onClick={() => { setEditing(item.id); setName(item.name) }}>수정</button><button onClick={() => remove(item.id)}>삭제</button></div></article>)}</div><form className="accordion-add" onSubmit={save}><input value={name} placeholder={`${label} ${editing ? '수정' : '추가'}`} onChange={e => setName(e.target.value)}/><button className="primary">{editing ? '수정 완료' : '추가'}</button>{editing && <button type="button" onClick={() => { setEditing(null); setName('') }}>취소</button>}</form></div></details>
}

function ScheduleAccordion() {
  const [open, setOpen] = useState(false), [items, setItems] = useState([]), [editing, setEditing] = useState(null)
  const blank = () => ({ title: '', start_date: today(), period_months: '' })
  const [form, setForm] = useState(blank)
  const load = async () => { const response = await fetch('/api/schedules/admin'); if (response.ok) setItems(await response.json()) }
  useEffect(() => { if (open) load() }, [open])
  const save = async event => { event.preventDefault(); const payload = { ...form, recurrence: 'NONE', period_months: form.period_months ? Number(form.period_months) : null }; const response = await fetch(editing ? `/api/schedules/${editing}` : '/api/schedules', { method: editing ? 'PUT' : 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) }); if (!response.ok) return alert(`스케줄 저장에 실패했습니다. (${response.status})`); setEditing(null); setForm(blank()); load() }
  const edit = item => { setEditing(item.id); setForm({ title: item.title, start_date: item.start_date, period_months: item.period_months || '' }) }
  const remove = async id => { if (!confirm('스케줄을 삭제할까요?')) return; const response = await fetch(`/api/schedules/${id}`, { method: 'DELETE' }); if (response.ok) load(); else alert('삭제에 실패했습니다.') }
  const setCompletion = async item => { const response = await fetch(`/api/schedules/${item.id}/completion`, { method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ completed: !item.completed }) }); if (response.ok) load(); else alert('완료 상태 변경에 실패했습니다.') }
  return <details className="admin-accordion" open={open} onToggle={event => setOpen(event.currentTarget.open)}><summary>스케줄러 <span>{open ? '⌃' : '⌄'}</span></summary><div className="accordion-content"><div className="accordion-list">{items.length === 0 && <p>저장된 일정이 없습니다.</p>}{items.map(item => <article key={item.id}><span>{item.title} · {item.start_date}{item.period_months ? ` · ${item.period_months}개월마다` : ''}</span><div><button className={item.completed ? 'active-toggle' : ''} onClick={() => setCompletion(item)}>{item.completed ? '완료' : '미완료'}</button><button onClick={() => edit(item)}>수정</button><button onClick={() => remove(item.id)}>삭제</button></div></article>)}</div><form className="schedule-admin-form" onSubmit={save}><input required placeholder="스케줄 내용" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })}/><input type="date" required value={form.start_date} onChange={e => setForm({ ...form, start_date: e.target.value })}/><input type="number" min="1" placeholder="반복 주기 (개월)" value={form.period_months} onChange={e => setForm({ ...form, period_months: e.target.value })}/><button className="primary">{editing ? '수정 완료' : '추가'}</button>{editing && <button type="button" onClick={() => { setEditing(null); setForm(blank()) }}>취소</button>}</form></div></details>
}

export function CatalogManager() {
  const [dark] = useState(() => localStorage.getItem('worklog-dark-mode') === 'true')
  useEffect(() => { document.body.classList.toggle('dark', dark) }, [dark])
  return <main className="catalog-page"><header className="catalog-header"><div><p className="eyebrow">SRGH IT Team</p><h1>관리자</h1></div><a className="admin-back-button" href="/">메인화면으로</a></header><section className="accordion-stack"><CatalogAccordion type="majors" label="메인 카테고리"/><CatalogAccordion type="minors" label="서브 카테고리"/><CatalogAccordion type="departments" label="부서"/><CatalogAccordion type="processors" label="처리자"/><ScheduleAccordion/></section></main>
}

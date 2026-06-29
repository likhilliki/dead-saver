import React, { useState, useEffect } from 'react';
import { 
  AlertTriangle, 
  Terminal, 
  ShieldAlert, 
  Mail, 
  FileCode, 
  FileText, 
  Calendar, 
  CheckCircle, 
  Trash2, 
  LogOut, 
  Plus, 
  ExternalLink,
  Github,
  Trello,
  Layers,
  Settings,
  Bell,
  Sparkles,
  Search,
  Play
} from 'lucide-react';
import { AreaChart, Area, XAxis, Tooltip, ResponsiveContainer } from 'recharts';

interface EventItem {
  id: string;
  title: string;
  source: string;
  startTime: string;
  location: string;
  status: 'pending' | 'resolved';
  priorityScore: number;
  taskCategory: 'Coding' | 'Writing' | 'Admin';
}

export default function App() {
  const [user, setUser] = useState<{ name: string; email: string } | null>({
    name: "Likhil Gowda",
    email: "likhilgowda89@gmail.com"
  });

  const [events, setEvents] = useState<EventItem[]>([
    {
      id: "1",
      title: "Critical Production Bug Hotfix",
      source: "GitHub",
      startTime: new Date(Date.now() + 18 * 3600 * 1000).toISOString(),
      location: "https://github.com/org/repo/issues/402",
      status: "pending",
      priorityScore: 9,
      taskCategory: "Coding"
    },
    {
      id: "2",
      title: "Unstop National Coding Challenge",
      source: "Unstop",
      startTime: new Date(Date.now() + 5 * 24 * 3600 * 1000).toISOString(),
      location: "https://unstop.com/hackathons/national-coding",
      status: "pending",
      priorityScore: 6,
      taskCategory: "Coding"
    },
    {
      id: "3",
      title: "Executive Whitepaper Pitch Draft",
      source: "Workspace Mail",
      startTime: new Date(Date.now() - 12 * 3600 * 1000).toISOString(), // overdue
      location: "Google Docs Folder #9",
      status: "pending",
      priorityScore: 8,
      taskCategory: "Writing"
    },
    {
      id: "4",
      title: "Quarterly Compliance Audit Submission",
      source: "Eventbrite / Admin Inbox",
      startTime: new Date(Date.now() + 24 * 3600 * 1000).toISOString(),
      location: "Main Conference Room",
      status: "resolved",
      priorityScore: 4,
      taskCategory: "Admin"
    }
  ]);

  const [integrations, setIntegrations] = useState({
    google: true,
    outlook: false,
    github: true
  });

  const [logs, setLogs] = useState<string[]>([
    "[SYSTEM] Dead-Saver Web Autonomous Core Activated.",
    "[SYSTEM] Ready to monitor client webhook inbox pipelines."
  ]);

  const [selectedEvent, setSelectedEvent] = useState<EventItem | null>(null);
  const [emailText, setEmailText] = useState("");
  const [showParserModal, setShowParserModal] = useState(false);
  const [isParsing, setIsParsing] = useState(false);
  const [isDrafting, setIsDrafting] = useState(false);
  const [negotiatorDraft, setNegotiatorDraft] = useState<{ subject: string; body: string } | null>(null);
  const [bulkSelectedIds, setBulkSelectedIds] = useState<string[]>([]);

  const addLog = (message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogs(prev => [`[${timestamp}] ${message}`, ...prev]);
  };

  const handleParseEmail = () => {
    if (!emailText.trim()) return;
    setIsParsing(true);
    addLog("[AGENT] Intercepting webhook email payload stream...");
    
    setTimeout(() => {
      // Intelligent parser mock
      const isCode = emailText.toLowerCase().includes("code") || emailText.toLowerCase().includes("bug");
      const isWrite = emailText.toLowerCase().includes("report") || emailText.toLowerCase().includes("draft") || emailText.toLowerCase().includes("pitch");
      
      const newEvent: EventItem = {
        id: Math.random().toString(),
        title: isCode ? "Emergency Server hotfix compiler build" : isWrite ? "Executive market expansion brief" : "Standard compliance request",
        source: emailText.toLowerCase().includes("github") ? "GitHub" : "Workspace Mail",
        startTime: new Date(Date.now() + 20 * 3600 * 1000).toISOString(),
        location: "https://workspace.office.com/task",
        status: "pending",
        priorityScore: 8,
        taskCategory: isCode ? "Coding" : isWrite ? "Writing" : "Admin"
      };

      setEvents(prev => [newEvent, ...prev]);
      addLog(`[AGENT] Triage parsed successfully: ${newEvent.title} [Priority: ${newEvent.priorityScore}/10]`);
      setIsParsing(false);
      setShowParserModal(false);
      setEmailText("");
    }, 1200);
  };

  const handlePanicButton = () => {
    addLog("[⚠️ PANIC ENGINE] Initiating 24-Hour Critical Deadlines Lockout Sequence...");
    const now = Date.now();
    let count = 0;

    events.forEach(event => {
      const remaining = new Date(event.startTime).getTime() - now;
      if (event.status === 'pending' && remaining > 0 && remaining <= 24 * 3600 * 1000) {
        count++;
        addLog(`[PANIC] Negotiator drafting extension request for: '${event.title}'`);
      }
    });

    if (count === 0) {
      addLog("[PANIC] Scan complete. No immediate deadlines due in 24 hours.");
    } else {
      addLog(`[PANIC] Lockout active. Drafted extensions for ${count} tasks.`);
    }
  };

  const handleDraftNegotiation = (event: EventItem) => {
    setIsDrafting(true);
    addLog("[AGENT] Requesting negotiator model assistance...");
    
    setTimeout(() => {
      setNegotiatorDraft({
        subject: `Extension Proposal request: [URGENT] ${event.title}`,
        body: `Dear Operations Team,\n\nI am writing to formally request a brief extension for the task '${event.title}'. Due to unexpected technical constraints and testing pipeline blockers, we require an additional 48 hours to complete delivery in accordance with top-grade standards.\n\nThank you for your consideration.\n\nBest regards,\n${user?.name || "Operations Lead"}`
      });
      addLog("[SUCCESS] Negotiation email drafted successfully.");
      setIsDrafting(false);
    }, 1000);
  };

  const handleMarkResolved = (event: EventItem) => {
    setEvents(prev => prev.map(e => e.id === event.id ? { ...e, status: 'resolved' } : e));
    addLog(`[SYSTEM] Task resolved: '${event.title}'`);
    if (selectedEvent?.id === event.id) {
      setSelectedEvent(prev => prev ? { ...prev, status: 'resolved' } : null);
    }
  };

  const handlePurge = (event: EventItem) => {
    setEvents(prev => prev.filter(e => e.id !== event.id));
    addLog(`[SYSTEM] Purged task: '${event.title}'`);
    setSelectedEvent(null);
  };

  const handleToggleSelectEvent = (id: string) => {
    setBulkSelectedIds(prev => prev.includes(id) ? prev.filter(x => x !== id) : [...prev, id]);
  };

  const handleSelectAllEvents = (filteredEvents: EventItem[]) => {
    setBulkSelectedIds(filteredEvents.map(e => e.id));
  };

  const handleClearBulkSelection = () => {
    setBulkSelectedIds([]);
  };

  const handleBulkResolve = () => {
    if (bulkSelectedIds.length === 0) return;
    setEvents(prev => prev.map(e => bulkSelectedIds.includes(e.id) ? { ...e, status: 'resolved' } : e));
    addLog(`[SYSTEM] Bulk Status Update: Resolved ${bulkSelectedIds.length} tasks.`);
    setSelectedEvent(prev => prev && bulkSelectedIds.includes(prev.id) ? { ...prev, status: 'resolved' } : prev);
    setBulkSelectedIds([]);
  };

  const handleBulkArchive = () => {
    if (bulkSelectedIds.length === 0) return;
    setEvents(prev => prev.filter(e => !bulkSelectedIds.includes(e.id)));
    addLog(`[SYSTEM] Bulk Purge: Archived/Deleted ${bulkSelectedIds.length} tasks.`);
    if (selectedEvent && bulkSelectedIds.includes(selectedEvent.id)) {
      setSelectedEvent(null);
    }
    setBulkSelectedIds([]);
  };

  const last7DaysData = React.useMemo(() => {
    const data = [];
    const now = new Date();
    for (let i = 6; i >= 0; i--) {
      const d = new Date();
      d.setDate(now.getDate() - i);
      const dateString = d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
      
      const dayStart = new Date(d);
      dayStart.setHours(0,0,0,0);
      const dayEnd = new Date(d);
      dayEnd.setHours(23,59,59,999);
      
      const dayEvents = events.filter(e => {
        const eDate = new Date(e.startTime);
        return eDate >= dayStart && eDate <= dayEnd;
      });
      
      const dayCount = dayEvents.length;
      const totalPriority = dayEvents.reduce((acc, curr) => acc + curr.priorityScore, 0);
      
      const baseWorkload = [3, 5, 4, 7, 5, 8, 4][(d.getDay()) % 7];
      const basePriority = [5.5, 6.8, 5.2, 7.5, 6.0, 7.0, 5.8][(d.getDay()) % 7];
      
      const finalWorkload = dayCount + baseWorkload;
      const finalPriority = dayCount > 0 
        ? parseFloat(((totalPriority + basePriority * baseWorkload) / (dayCount + baseWorkload)).toFixed(1))
        : basePriority;
        
      data.push({
        name: i === 0 ? "Today" : dateString,
        Workload: finalWorkload,
        Priority: finalPriority
      });
    }
    return data;
  }, [events]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-cyan-500 selection:text-black">
      {/* Top Header */}
      <header className="border-b border-slate-800 bg-slate-900/60 backdrop-blur px-6 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <AlertTriangle className="text-cyan-400 w-8 h-8 animate-pulse" />
          <div>
            <h1 className="text-xl font-black tracking-wider text-cyan-400 font-mono">DEAD-SAVER</h1>
            <p className="text-xs text-slate-400 font-mono">AUTONOMOUS ZERO-INPUT CRISIS GATEWAY</p>
          </div>
        </div>

        <div>
          {user ? (
            <div className="flex items-center gap-4">
              <div className="text-right">
                <p className="text-sm font-bold text-slate-200">{user.name}</p>
                <p className="text-xs text-slate-400 font-mono">{user.email}</p>
              </div>
              <button 
                onClick={() => setUser(null)}
                className="p-2 text-slate-400 hover:text-red-400 transition"
                title="Sign Out"
              >
                <LogOut size={18} />
              </button>
            </div>
          ) : (
            <button 
              onClick={() => setUser({ name: "Likhil Gowda", email: "likhilgowda89@gmail.com" })}
              className="bg-cyan-500 text-black px-4 py-2 rounded-lg font-bold hover:bg-cyan-400 transition flex items-center gap-2"
            >
              Sign In with Google
            </button>
          )}
        </div>
      </header>

      {/* Main Body Grid */}
      <main className="flex-1 p-6 grid grid-cols-1 lg:grid-cols-4 gap-6 max-w-7xl mx-auto w-full">
        {/* Left 3 columns */}
        <div className="lg:col-span-3 flex flex-col gap-6">
          
          {/* Header Dashboard Summary */}
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-xl grid grid-cols-1 md:grid-cols-3 gap-6 items-center">
            <div className="md:col-span-1 flex flex-col justify-between h-full">
              <div>
                <p className="text-xs text-slate-400 font-mono uppercase tracking-wider">Triage Ops Status</p>
                <div className="flex flex-col gap-2 mt-2">
                  <span className="flex items-center gap-2 text-lg font-extrabold text-slate-200">
                    <span className="w-3 h-3 rounded-full bg-amber-500 inline-block animate-ping"></span>
                    {events.filter(e => e.status === 'pending').length} Active Crisis Buffer
                  </span>
                  <span className="text-sm text-red-400 font-bold flex items-center gap-2">
                    <span className="w-2 h-2 rounded-full bg-red-500"></span>
                    {events.filter(e => e.status === 'pending' && e.priorityScore >= 8).length} Urgent Lockouts
                  </span>
                </div>
              </div>
              <button 
                onClick={() => setShowParserModal(true)}
                className="mt-4 bg-cyan-500 text-black hover:bg-cyan-400 px-4 py-2.5 rounded-lg font-bold transition flex items-center justify-center gap-2 text-sm w-full md:w-auto md:self-start"
              >
                <Mail size={16} />
                Triage Raw Email
              </button>
            </div>

            <div className="md:col-span-2 h-36 bg-slate-950/50 border border-slate-800/80 rounded-xl p-3 flex flex-col justify-between">
              <div className="flex items-center justify-between px-1">
                <span className="text-[10px] font-mono text-cyan-400 font-bold uppercase tracking-wider flex items-center gap-2">
                  <span className="w-1.5 h-1.5 rounded-full bg-cyan-400"></span>
                  7-Day Trend: Workload Volume &amp; Avg Priority
                </span>
                <div className="flex items-center gap-3 text-[10px] font-mono">
                  <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-sm bg-cyan-400"></span> Volume</span>
                  <span className="flex items-center gap-1"><span className="w-2 h-2 rounded-sm bg-red-400"></span> Avg Priority</span>
                </div>
              </div>
              
              <div className="w-full h-24 mt-1">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={last7DaysData} margin={{ top: 5, right: 5, left: 5, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorWorkload" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#22D3EE" stopOpacity={0.2}/>
                        <stop offset="95%" stopColor="#22D3EE" stopOpacity={0}/>
                      </linearGradient>
                      <linearGradient id="colorPriority" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#EF4444" stopOpacity={0.15}/>
                        <stop offset="95%" stopColor="#EF4444" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <XAxis 
                      dataKey="name" 
                      stroke="#475569" 
                      fontSize={8}
                      tickLine={false}
                      axisLine={false}
                    />
                    <Tooltip 
                      contentStyle={{ backgroundColor: '#090d16', borderColor: '#1e293b', borderRadius: '8px' }}
                      labelStyle={{ color: '#94a3b8', fontSize: '10px', fontFamily: 'monospace' }}
                      itemStyle={{ fontSize: '10px', fontFamily: 'monospace', padding: '1px 0' }}
                    />
                    <Area 
                      type="monotone" 
                      dataKey="Workload" 
                      stroke="#22D3EE" 
                      strokeWidth={2}
                      fillOpacity={1} 
                      fill="url(#colorWorkload)" 
                      name="Vol"
                    />
                    <Area 
                      type="monotone" 
                      dataKey="Priority" 
                      stroke="#EF4444" 
                      strokeWidth={1.5}
                      strokeDasharray="3 3"
                      fillOpacity={1} 
                      fill="url(#colorPriority)" 
                      name="Prio"
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>
          </div>

          {/* Terminal Console log monitor */}
          <div className="bg-black border border-slate-800 p-4 rounded-xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-2 mb-2">
              <div className="flex items-center gap-2">
                <Terminal className="text-green-400 w-4 h-4" />
                <span className="text-xs font-mono text-green-400 font-bold uppercase tracking-wider">Automated Operations Terminal logs</span>
              </div>
              <span className="text-xs text-green-500 font-mono">STATUS: ACTIVE</span>
            </div>
            <div className="max-h-24 overflow-y-auto font-mono text-xs space-y-1">
              {logs.map((log, i) => (
                <p key={i} className="text-slate-300">
                  <span className="text-cyan-500 font-bold">&gt;</span> {log}
                </p>
              ))}
            </div>
          </div>

          {/* Panel D: Master Panic button */}
          <div className="bg-slate-900 border border-red-500/30 p-4 rounded-xl flex items-center justify-between">
            <div className="flex items-center gap-4">
              <ShieldAlert className="text-red-500 w-10 h-10 animate-bounce" />
              <div>
                <h3 className="text-sm font-black font-mono text-red-400 uppercase tracking-wider">Panel D: Master Panic System Redirect</h3>
                <p className="text-xs text-slate-400 max-w-lg">Identifies tasks due in under 24 hours and prepares extension drafts using negotiator agents.</p>
              </div>
            </div>
            <button 
              onClick={handlePanicButton}
              className="bg-red-500 text-black hover:bg-red-400 px-6 py-3 rounded-lg font-black text-xs uppercase tracking-widest transition"
            >
              Trigger Imminent Extensions
            </button>
          </div>

          {/* Panels A & B Container */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            
            {/* Panel B: Triage queue (priority sorted) */}
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
              <div className="flex items-center justify-between mb-3">
                <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider">Panel B: Triage Queue</h2>
                {events.length > 0 && (
                  <button
                    onClick={() => {
                      const allSelected = bulkSelectedIds.length === events.length;
                      if (allSelected) {
                        handleClearBulkSelection();
                      } else {
                        handleSelectAllEvents(events);
                      }
                    }}
                    className="text-[10px] font-mono font-bold text-slate-500 hover:text-cyan-400 uppercase transition"
                  >
                    {bulkSelectedIds.length === events.length ? 'Deselect All' : 'Select All'}
                  </button>
                )}
              </div>

              {/* Bulk Action Bar */}
              {bulkSelectedIds.length > 0 && (
                <div className="mb-3 p-3 bg-slate-950 border border-cyan-500/30 rounded-lg flex items-center justify-between gap-3">
                  <span className="text-xs font-mono font-bold text-cyan-400">
                    {bulkSelectedIds.length} Selected
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={handleBulkResolve}
                      className="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-green-400 bg-green-500/10 border border-green-500/30 rounded hover:bg-green-500/20 transition"
                    >
                      Resolve
                    </button>
                    <button
                      onClick={handleBulkArchive}
                      className="px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-red-400 bg-red-500/10 border border-red-500/30 rounded hover:bg-red-500/20 transition"
                    >
                      Archive
                    </button>
                    <button
                      onClick={handleClearBulkSelection}
                      className="px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-slate-400 hover:text-slate-200 transition"
                    >
                      Cancel
                    </button>
                  </div>
                </div>
              )}

              <div className="space-y-3">
                {events.sort((a,b) => b.priorityScore - a.priorityScore).map(event => {
                  const isUrgent = event.priorityScore >= 8 && event.status === 'pending';
                  const isChecked = bulkSelectedIds.includes(event.id);
                  return (
                    <div 
                      key={event.id}
                      className={`p-3 rounded-lg border transition flex items-start gap-3 ${selectedEvent?.id === event.id ? 'border-cyan-400 bg-slate-800/40' : 'border-slate-800 bg-slate-950'}`}
                    >
                      <input
                        type="checkbox"
                        checked={isChecked}
                        onChange={(e) => {
                          e.stopPropagation();
                          handleToggleSelectEvent(event.id);
                        }}
                        className="mt-1 accent-cyan-400 cursor-pointer w-4 h-4 rounded border-slate-700 bg-slate-950 text-cyan-400 focus:ring-cyan-500"
                      />
                      <div 
                        onClick={() => setSelectedEvent(event)}
                        className="flex-1 cursor-pointer"
                      >
                        <div className="flex items-center justify-between mb-1">
                          <span className={`text-xs px-2 py-0.5 rounded font-bold ${isUrgent ? 'bg-red-500/20 text-red-400 border border-red-500' : 'bg-slate-800 text-slate-400'}`}>
                            Prio {event.priorityScore}/10
                          </span>
                          <span className="text-[10px] text-slate-400 uppercase tracking-widest">{event.taskCategory}</span>
                        </div>
                        <h4 className="text-sm font-bold text-slate-200">{event.title}</h4>
                        <p className="text-xs text-cyan-400 font-mono mt-1">{event.source}</p>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Panel A: Timeline Grid */}
            <div className="bg-slate-900 border border-slate-800 p-4 rounded-xl">
              <h2 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider mb-3">Panel A: Unified Timeline</h2>
              <div className="space-y-3">
                {events.sort((a,b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()).map(event => {
                  return (
                    <div 
                      key={event.id}
                      onClick={() => setSelectedEvent(event)}
                      className={`p-3 rounded-lg border cursor-pointer transition ${selectedEvent?.id === event.id ? 'border-cyan-400 bg-slate-800/40' : 'border-slate-800 hover:border-slate-700 bg-slate-950'}`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs text-cyan-400 font-bold uppercase tracking-wider">{event.source}</span>
                        <span className="text-xs text-slate-400">{new Date(event.startTime).toLocaleDateString()}</span>
                      </div>
                      <h4 className="text-sm font-bold text-slate-200">{event.title}</h4>
                      <p className="text-xs text-slate-500 mt-1 truncate">{event.location}</p>
                    </div>
                  );
                })}
              </div>
            </div>

          </div>

          {/* Panel E: Integrations Dashboard */}
          <div className="bg-slate-900 border border-slate-800 p-5 rounded-xl">
            <h3 className="text-sm font-mono font-bold text-cyan-400 uppercase tracking-wider mb-4">Panel E: Secure Integrations toggles</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {[
                { id: 'google', label: 'Google Workspace', status: integrations.google },
                { id: 'outlook', label: 'Microsoft Outlook', status: integrations.outlook },
                { id: 'github', label: 'GitHub Webhooks', status: integrations.github }
              ].map(integration => (
                <div key={integration.id} className="bg-slate-950 p-4 border border-slate-800 rounded-lg flex items-center justify-between">
                  <div>
                    <p className="text-sm font-bold">{integration.label}</p>
                    <p className="text-xs text-slate-400 font-mono">{integration.status ? "CONNECTED" : "OFFLINE"}</p>
                  </div>
                  <input 
                    type="checkbox" 
                    checked={integration.status} 
                    onChange={() => {
                      setIntegrations(prev => ({
                        ...prev,
                        [integration.id]: !prev[integration.id as keyof typeof integrations]
                      }));
                      addLog(`Toggle connection state: ${integration.label} -> ${!integration.status ? "ACTIVE" : "STANDBY"}`);
                    }}
                    className="w-4 h-4 text-cyan-500 bg-slate-950 border-slate-800 rounded focus:ring-cyan-500"
                  />
                </div>
              ))}
            </div>
          </div>

        </div>

        {/* Right column: Panel C: Drawer */}
        <div className="lg:col-span-1 bg-slate-900 border border-slate-800 rounded-xl p-5 flex flex-col justify-between">
          {selectedEvent ? (
            <div className="flex flex-col gap-6">
              <div>
                <h2 className="text-xs font-mono font-bold text-cyan-400 uppercase tracking-widest mb-4">Panel C: Contextual Toolbelt</h2>
                <div className="bg-slate-950 p-4 border border-slate-800 rounded-lg space-y-3">
                  <h3 className="text-md font-bold text-slate-100">{selectedEvent.title}</h3>
                  <hr className="border-slate-800" />
                  <p className="text-xs text-slate-400">Category: <span className="font-bold text-cyan-400">{selectedEvent.taskCategory}</span></p>
                  <p className="text-xs text-slate-400">Expires: <span className="font-bold text-slate-300">{new Date(selectedEvent.startTime).toLocaleString()}</span></p>
                  <p className="text-xs text-slate-400">Location: <span className="text-cyan-400 underline cursor-pointer">{selectedEvent.location}</span></p>
                </div>
              </div>

              {/* Context Utilities based on task category */}
              <div>
                <h4 className="text-xs font-mono font-bold text-slate-400 mb-2">Context Utilities</h4>
                {selectedEvent.taskCategory === 'Coding' && (
                  <div className="space-y-2">
                    <a 
                      href={`https://stackoverflow.com/search?q=android+compile+${selectedEvent.title}`}
                      target="_blank"
                      rel="noreferrer"
                      className="bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs py-2 px-3 rounded-lg flex items-center justify-between transition"
                    >
                      StackOverflow diagnostic query
                      <ExternalLink size={12} />
                    </a>
                    <a 
                      href="https://play.kotlinlang.org"
                      target="_blank"
                      rel="noreferrer"
                      className="bg-cyan-500 hover:bg-cyan-400 text-black font-bold text-xs py-2 px-3 rounded-lg flex items-center justify-between transition"
                    >
                      Open Kotlin Sandbox Compiler
                      <ExternalLink size={12} />
                    </a>
                  </div>
                )}

                {selectedEvent.taskCategory === 'Writing' && (
                  <div className="space-y-2">
                    <button 
                      onClick={() => navigator.clipboard.writeText(selectedEvent.title)}
                      className="bg-cyan-500 text-black hover:bg-cyan-400 font-bold text-xs py-2 px-3 rounded-lg w-full text-left flex items-center justify-between transition"
                    >
                      Copy Title for Documents
                    </button>
                    <a 
                      href="https://docs.google.com"
                      target="_blank"
                      rel="noreferrer"
                      className="bg-slate-800 hover:bg-slate-700 font-bold text-xs py-2 px-3 rounded-lg flex items-center justify-between transition"
                    >
                      Google Docs Cloud Drive
                      <ExternalLink size={12} />
                    </a>
                  </div>
                )}

                {selectedEvent.taskCategory === 'Admin' && (
                  <button 
                    onClick={() => navigator.clipboard.writeText(selectedEvent.location)}
                    className="bg-cyan-500 text-black hover:bg-cyan-400 font-bold text-xs py-2 px-3 rounded-lg w-full text-left transition"
                  >
                    Copy Location Details
                  </button>
                )}
              </div>

              {/* General details */}
              <div className="space-y-2">
                <button 
                  onClick={() => addLog(`Sync triggers: Sent event to Google Calendar API.`)}
                  className="bg-green-600 hover:bg-green-500 text-black font-black text-xs py-2 px-3 rounded-lg w-full transition flex items-center justify-center gap-2"
                >
                  <Calendar size={14} />
                  Schedule on Google Calendar
                </button>

                <button 
                  onClick={() => handleDraftNegotiation(selectedEvent)}
                  className="bg-amber-500 hover:bg-amber-400 text-black font-black text-xs py-2 px-3 rounded-lg w-full transition flex items-center justify-center gap-2"
                >
                  <Sparkles size={14} />
                  {isDrafting ? "Drafting..." : "Draft Negotiation Extension"}
                </button>
              </div>

              <div className="space-y-2">
                <button 
                  onClick={() => handleMarkResolved(selectedEvent)}
                  className="w-full bg-slate-800 hover:bg-slate-700 text-green-400 font-bold text-xs py-2 rounded-lg transition"
                >
                  Mark Task as Resolved
                </button>
                <button 
                  onClick={() => handlePurge(selectedEvent)}
                  className="w-full bg-slate-800 hover:bg-slate-700 text-red-400 text-xs py-2 rounded-lg transition flex items-center justify-center gap-2"
                >
                  <Trash2 size={12} />
                  Purge Crisis Queue
                </button>
              </div>
            </div>
          ) : (
            <div className="text-center py-20 text-slate-500 flex flex-col items-center gap-4">
              <Layers size={36} />
              <p className="text-xs font-mono">Select a crisis triage item to mount Panel C Contextual Toolbelt</p>
            </div>
          )}

          {negotiatorDraft && (
            <div className="mt-4 bg-slate-950 p-3 border border-amber-500/30 rounded-lg space-y-2">
              <p className="text-[10px] font-mono text-amber-500 font-bold uppercase">Negotiator Extension Proposal</p>
              <p className="text-xs font-bold text-slate-300">Sub: {negotiatorDraft.subject}</p>
              <button 
                onClick={() => {
                  navigator.clipboard.writeText(negotiatorDraft.body);
                  addLog("Copied AI negotiator extension email draft to clipboard!");
                  setNegotiatorDraft(null);
                }}
                className="bg-amber-500 text-black text-[10px] font-black px-2 py-1 rounded hover:bg-amber-400 transition"
              >
                Copy Draft body
              </button>
            </div>
          )}
        </div>
      </main>

      {/* Parser email Modal */}
      {showParserModal && (
        <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 backdrop-blur-sm z-50">
          <div className="bg-slate-900 border border-slate-800 p-6 rounded-xl max-w-lg w-full space-y-4">
            <h3 className="text-md font-bold font-mono text-cyan-400">Parse Raw email inbox stream</h3>
            <p className="text-xs text-slate-400">Paste unformatted confirmation email text or deadlines details. Gemini AI extracts values directly.</p>
            <textarea 
              value={emailText}
              onChange={(e) => setEmailText(e.target.value)}
              placeholder="Paste raw email message details here..."
              className="w-full h-32 bg-slate-950 border border-slate-800 rounded-lg p-3 text-xs font-mono text-slate-300 focus:outline-none focus:border-cyan-400"
            />
            <div className="flex gap-2 justify-end">
              <button 
                onClick={() => setShowParserModal(false)}
                className="px-3 py-1.5 rounded-lg text-xs font-bold text-slate-400 hover:text-slate-200 transition"
              >
                Cancel
              </button>
              <button 
                onClick={handleParseEmail}
                className="bg-cyan-500 text-black px-4 py-1.5 rounded-lg text-xs font-black transition hover:bg-cyan-400"
              >
                {isParsing ? "Triage Parsing..." : "Initiate Autonomous Triage"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

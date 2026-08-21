(() => {
  const TOOLS = [
    { code: "log", label: "日志搜索" },
    { code: "sql", label: "业务查询" },
    { code: "repo_read", label: "读代码" },
    { code: "repo_change", label: "改代码" },
  ];

  const state = {
    page: "prompt",
    prompt: { current: 1, size: 10, total: 0, keyword: "" },
    bot: { current: 1, size: 10, total: 0, keyword: "" },
    change: { current: 1, size: 10, total: 0, keyword: "" },
    turn: { current: 1, size: 10, total: 0, keyword: "" },
    drawer: null,
    confirm: null,
  };

  function qs(id) {
    return document.getElementById(id);
  }

  function escapeHtml(value) {
    return String(value ?? "")
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function toast(message, isError) {
    const el = qs("toast");
    el.textContent = message;
    el.classList.toggle("is-error", Boolean(isError));
    el.classList.remove("is-hidden");
    clearTimeout(toast._timer);
    toast._timer = setTimeout(() => el.classList.add("is-hidden"), 2400);
  }

  function unwrap(json) {
    if (json && typeof json === "object" && "data" in json) {
      return json.data;
    }
    return json;
  }

  function isOk(json) {
    if (json == null) {
      return false;
    }
    if (typeof json !== "object") {
      return true;
    }
    const code = json.code;
    return code === undefined || code === 200 || code === 0 || code === "200";
  }

  function errorMessage(json, fallback) {
    return json?.message || json?.msg || fallback || "请求失败";
  }

  function pageRecords(data) {
    if (!data) {
      return { records: [], total: 0 };
    }
    if (Array.isArray(data)) {
      return { records: data, total: data.length };
    }
    const records = data.records || data.data || data.list || [];
    return { records, total: Number(data.total ?? records.length) };
  }

  async function request(url, options = {}) {
    const response = await fetch(url, {
      headers: { Accept: "application/json", ...(options.body ? { "Content-Type": "application/json" } : {}) },
      ...options,
    });
    let json = null;
    try {
      json = await response.json();
    } catch {
      json = null;
    }
    if (!response.ok || !isOk(json)) {
      throw new Error(errorMessage(json, `HTTP ${response.status}`));
    }
    return unwrap(json);
  }

  function emptyRow(colspan, text) {
    return `<tr><td colspan="${colspan}" class="empty">${escapeHtml(text)}</td></tr>`;
  }

  function tagEnabled(on) {
    return on
      ? '<span class="tag tag-ok">启用</span>'
      : '<span class="tag tag-off">禁用</span>';
  }

  function renderPager(el, pageState, onChange) {
    const pages = Math.max(1, Math.ceil(pageState.total / pageState.size));
    el.innerHTML = `
      <span>共 ${pageState.total} 条</span>
      <button class="btn" type="button" data-act="prev" ${pageState.current <= 1 ? "disabled" : ""}>上一页</button>
      <span>${pageState.current} / ${pages}</span>
      <button class="btn" type="button" data-act="next" ${pageState.current >= pages ? "disabled" : ""}>下一页</button>
    `;
    el.querySelector("[data-act=prev]")?.addEventListener("click", () => {
      pageState.current -= 1;
      onChange();
    });
    el.querySelector("[data-act=next]")?.addEventListener("click", () => {
      pageState.current += 1;
      onChange();
    });
  }

  function openDrawer(title, html, onSave, options = {}) {
    qs("drawer-title").textContent = title;
    qs("drawer-body").innerHTML = html;
    qs("drawer").classList.toggle("drawer-wide", Boolean(options.wide));
    qs("drawer-save").classList.toggle("is-hidden", Boolean(options.hideSave) || !onSave);
    qs("drawer-extra").classList.toggle("is-hidden", !options.onExtra);
    qs("drawer-extra").textContent = options.extraLabel || "应用这次修改";
    qs("drawer").classList.add("is-open");
    qs("drawer").setAttribute("aria-hidden", "false");
    qs("drawer-mask").classList.remove("is-hidden");
    state.drawer = { onSave, onExtra: options.onExtra };
  }

  function closeDrawer() {
    qs("drawer").classList.remove("is-open");
    qs("drawer").setAttribute("aria-hidden", "true");
    qs("drawer-mask").classList.add("is-hidden");
    qs("drawer-extra").classList.add("is-hidden");
    qs("drawer").classList.remove("drawer-wide");
    state.drawer = null;
  }

  function confirmAction(title, text, okLabel, onOk, danger) {
    qs("confirm-title").textContent = title;
    qs("confirm-text").textContent = text;
    qs("confirm-ok").textContent = okLabel || "确定";
    qs("confirm-ok").classList.toggle("btn-danger", Boolean(danger));
    qs("confirm-mask").classList.remove("is-hidden");
    state.confirm = { onOk };
  }

  function confirmDelete(text, onOk) {
    confirmAction("确认删除", text, "删除", onOk, true);
  }

  function closeConfirm() {
    qs("confirm-mask").classList.add("is-hidden");
    state.confirm = null;
  }

  function formValue(id) {
    return qs(id)?.value?.trim() ?? "";
  }

  async function loadPrompt() {
    const tbody = qs("prompt-tbody");
    tbody.innerHTML = emptyRow(6, "加载中…");
    const params = new URLSearchParams({
      current: String(state.prompt.current),
      size: String(state.prompt.size),
    });
    const keyword = state.prompt.keyword;
    if (keyword) {
      params.set("templateName", keyword);
    }
    try {
      const { records, total } = pageRecords(await request(`/ai/prompt-template/page?${params}`));
      state.prompt.total = total;
      if (!records.length) {
        tbody.innerHTML = emptyRow(6, "暂无数据");
      } else {
        tbody.innerHTML = records
          .map((row) => {
            const id = row.id;
            return `<tr>
              <td>${escapeHtml(row.templateName)}</td>
              <td>${escapeHtml(row.templateCode)}</td>
              <td>${escapeHtml(row.category || "—")}</td>
              <td>${escapeHtml(row.variables || "—")}</td>
              <td>${tagEnabled(Number(row.status) === 1)}</td>
              <td class="col-op">
                <button class="btn-link" data-edit="${id}">编辑</button>
                <button class="btn-link danger" data-del="${id}" data-name="${escapeHtml(row.templateName || row.templateCode)}">删除</button>
              </td>
            </tr>`;
          })
          .join("");
        tbody.querySelectorAll("[data-edit]").forEach((btn) => {
          btn.addEventListener("click", () => openPromptForm(records.find((r) => String(r.id) === btn.dataset.edit)));
        });
        tbody.querySelectorAll("[data-del]").forEach((btn) => {
          btn.addEventListener("click", () =>
            confirmDelete(`确定删除模板「${btn.dataset.name}」？`, async () => {
              await request(`/ai/prompt-template/${btn.dataset.del}`, { method: "DELETE" });
              toast("已删除");
              await loadPrompt();
            }),
          );
        });
      }
      renderPager(qs("prompt-pager"), state.prompt, loadPrompt);
    } catch (error) {
      tbody.innerHTML = emptyRow(6, error.message);
    }
  }

  function openPromptForm(row) {
    const isEdit = Boolean(row?.id);
    openDrawer(
      isEdit ? "编辑模板" : "新建模板",
      `
      <div class="form-row">
        <div class="form-item">
          <label class="req">模板编码</label>
          <input id="f-templateCode" class="input" ${isEdit ? "disabled" : ""} value="${escapeHtml(row?.templateCode || "")}" placeholder="STOCK_DAILY_QUERY" />
        </div>
        <div class="form-item">
          <label class="req">模板名称</label>
          <input id="f-templateName" class="input" value="${escapeHtml(row?.templateName || "")}" placeholder="股票日线信息查询" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-item">
          <label>分类</label>
          <select id="f-category" class="select">
            ${["", "通用", "金融", "股票"]
              .map((item) => `<option value="${item}" ${row?.category === item ? "selected" : ""}>${item || "未选择"}</option>`)
              .join("")}
          </select>
        </div>
        <div class="form-item">
          <label>状态</label>
          <select id="f-status" class="select">
            <option value="1" ${Number(row?.status ?? 1) === 1 ? "selected" : ""}>启用</option>
            <option value="0" ${Number(row?.status) === 0 ? "selected" : ""}>禁用</option>
          </select>
        </div>
      </div>
      <div class="form-item" style="margin-bottom:14px">
        <label>变量</label>
        <input id="f-variables" class="input" value="${escapeHtml(row?.variables || "")}" placeholder="stock_code, start_date" />
      </div>
      <div class="form-item" style="margin-bottom:14px">
        <label>描述</label>
        <input id="f-description" class="input" value="${escapeHtml(row?.description || "")}" />
      </div>
      <div class="form-item">
        <label class="req">模板内容</label>
        <textarea id="f-templateContent" class="textarea" placeholder="支持 {var} 占位">${escapeHtml(row?.templateContent || "")}</textarea>
      </div>
    `,
      async () => {
        const payload = {
          id: row?.id,
          templateCode: formValue("f-templateCode"),
          templateName: formValue("f-templateName"),
          category: formValue("f-category") || null,
          variables: formValue("f-variables") || null,
          description: formValue("f-description") || null,
          status: Number(qs("f-status").value),
          templateContent: qs("f-templateContent").value,
        };
        if (!payload.templateCode || !payload.templateName || !payload.templateContent.trim()) {
          throw new Error("请填写编码、名称和内容");
        }
        await request("/ai/prompt-template", {
          method: isEdit ? "PUT" : "POST",
          body: JSON.stringify(payload),
        });
        toast(isEdit ? "已保存" : "已创建");
        await loadPrompt();
      },
    );
  }

  async function loadBot() {
    const tbody = qs("bot-tbody");
    tbody.innerHTML = emptyRow(8, "加载中…");
    const params = new URLSearchParams({
      current: String(state.bot.current),
      size: String(state.bot.size),
    });
    const keyword = state.bot.keyword;
    if (keyword) {
      params.set("botName", keyword);
    }
    try {
      const { records, total } = pageRecords(await request(`/ai/ai-bot-config/page?${params}`));
      state.bot.total = total;
      if (!records.length) {
        tbody.innerHTML = emptyRow(8, "暂无数据");
      } else {
        tbody.innerHTML = records
          .map((row) => `<tr>
            <td>${escapeHtml(row.botName)}</td>
            <td>${escapeHtml(row.botCode)}</td>
            <td>${escapeHtml(row.botId)}</td>
            <td>${escapeHtml(row.channelType || "WEIXIN")}</td>
            <td>${escapeHtml(row.toolCodes || "—")}</td>
            <td>${row.hasSecret ? '<span class="tag tag-ok">已配置</span>' : '<span class="tag tag-muted">未配置</span>'}</td>
            <td>${tagEnabled(Number(row.isEnabled) === 1)}</td>
            <td class="col-op">
              <button class="btn-link" data-edit="${row.id}">编辑</button>
              <button class="btn-link danger" data-del="${row.id}" data-name="${escapeHtml(row.botName || row.botCode)}">删除</button>
            </td>
          </tr>`)
          .join("");
        tbody.querySelectorAll("[data-edit]").forEach((btn) => {
          btn.addEventListener("click", async () => {
            const detail = await request(`/ai/ai-bot-config/${btn.dataset.edit}`);
            openBotForm(detail);
          });
        });
        tbody.querySelectorAll("[data-del]").forEach((btn) => {
          btn.addEventListener("click", () =>
            confirmDelete(`确定删除机器人「${btn.dataset.name}」？`, async () => {
              await request(`/ai/ai-bot-config/${btn.dataset.del}`, { method: "DELETE" });
              toast("已删除");
              await loadBot();
            }),
          );
        });
      }
      renderPager(qs("bot-pager"), state.bot, loadBot);
    } catch (error) {
      tbody.innerHTML = emptyRow(8, error.message);
    }
  }

  function openBotForm(row) {
    const isEdit = Boolean(row?.id);
    const selected = new Set(String(row?.toolCodes || "log,sql,repo_read").split(/[,;\s]+/).filter(Boolean));
    openDrawer(
      isEdit ? "编辑机器人" : "新建机器人",
      `
      <div class="form-row">
        <div class="form-item">
          <label class="req">内部编码</label>
          <input id="f-botCode" class="input" ${isEdit ? "disabled" : ""} value="${escapeHtml(row?.botCode || "")}" placeholder="supply-chain" />
        </div>
        <div class="form-item">
          <label class="req">名称</label>
          <input id="f-botName" class="input" value="${escapeHtml(row?.botName || "")}" placeholder="供应链排障助手" />
        </div>
      </div>
      <div class="form-row">
        <div class="form-item">
          <label class="req">企微 BotId</label>
          <input id="f-botId" class="input" value="${escapeHtml(row?.botId || "")}" />
        </div>
        <div class="form-item">
          <label>长连接 Secret</label>
          <input id="f-secret" class="input" type="password" autocomplete="new-password" placeholder="${isEdit ? "留空则不修改" : "可选"}" />
          <div class="hint">${row?.hasSecret ? "已配置密钥，仅在需要轮换时填写" : "未配置时即使启用也不会建连"}</div>
        </div>
      </div>
      <div class="form-row">
        <div class="form-item">
          <label>通道</label>
          <select id="f-channelType" class="select">
            <option value="WEIXIN" ${row?.channelType !== "FEISHU" ? "selected" : ""}>企微 WEIXIN</option>
            <option value="FEISHU" ${row?.channelType === "FEISHU" ? "selected" : ""}>飞书 FEISHU</option>
          </select>
        </div>
        <div class="form-item">
          <label>启用</label>
          <select id="f-isEnabled" class="select">
            <option value="1" ${Number(row?.isEnabled) === 1 ? "selected" : ""}>启用</option>
            <option value="0" ${Number(row?.isEnabled ?? 0) !== 1 ? "selected" : ""}>禁用</option>
          </select>
        </div>
      </div>
      <div class="form-item" style="margin-bottom:14px">
        <label>工具集</label>
        <div class="checks">
          ${TOOLS.map(
            (tool) => `<label><input type="checkbox" name="tool" value="${tool.code}" ${selected.has(tool.code) ? "checked" : ""} /> ${tool.label} (${tool.code})</label>`,
          ).join("")}
        </div>
      </div>
      <div class="form-item" style="margin-bottom:14px">
        <label>欢迎语</label>
        <input id="f-welcomeText" class="input" value="${escapeHtml(row?.welcomeText || "")}" />
      </div>
      <div class="form-item" style="margin-bottom:14px">
        <label>描述</label>
        <input id="f-botDescription" class="input" value="${escapeHtml(row?.description || "")}" />
      </div>
      <div class="form-item">
        <label class="req">系统提示词</label>
        <textarea id="f-systemPrompt" class="textarea">${escapeHtml(row?.systemPrompt || "")}</textarea>
      </div>
    `,
      async () => {
        const tools = [...document.querySelectorAll('input[name="tool"]:checked')].map((el) => el.value);
        const payload = {
          id: row?.id,
          botCode: formValue("f-botCode"),
          botName: formValue("f-botName"),
          botId: formValue("f-botId"),
          secret: formValue("f-secret"),
          channelType: qs("f-channelType").value,
          isEnabled: Number(qs("f-isEnabled").value),
          toolCodes: tools.join(","),
          welcomeText: formValue("f-welcomeText"),
          description: formValue("f-botDescription"),
          systemPrompt: qs("f-systemPrompt").value,
        };
        if (!payload.botCode || !payload.botName || !payload.botId || !payload.systemPrompt.trim()) {
          throw new Error("请填写编码、名称、BotId 和系统提示词");
        }
        await request("/ai/ai-bot-config", {
          method: isEdit ? "PUT" : "POST",
          body: JSON.stringify(payload),
        });
        toast(isEdit ? "已保存" : "已创建");
        await loadBot();
      },
    );
  }

  function formatDuration(ms) {
    if (ms == null || ms === "") {
      return "—";
    }
    const n = Number(ms);
    if (Number.isNaN(n)) {
      return "—";
    }
    if (n < 1000) {
      return `${n}ms`;
    }
    return `${(n / 1000).toFixed(1)}s`;
  }

  function turnStatusTag(status) {
    if (Number(status) === 1) {
      return '<span class="tag tag-ok">成功</span>';
    }
    if (Number(status) === 2) {
      return '<span class="tag tag-off">失败</span>';
    }
    if (Number(status) === 3) {
      return '<span class="tag tag-off">超时</span>';
    }
    return '<span class="tag tag-muted">运行中</span>';
  }

  function toolLabel(code) {
    return TOOLS.find((item) => item.code === code)?.label || code || "工具";
  }

  async function listTurnsBySession(sessionId) {
    if (!sessionId) {
      toast("缺少 sessionId", true);
      return;
    }
    const params = new URLSearchParams({
      current: "1",
      size: "20",
      sessionId,
    });
    const { records } = pageRecords(await request(`/ai/ai-agent-turn/page?${params}`));
    const html = `
      <dl class="meta-grid">
        <dt>Session ID</dt><dd class="mono">${escapeHtml(sessionId)}</dd>
        <dt>轮次数</dt><dd>${escapeHtml(records.length)}</dd>
      </dl>
      ${records.length
        ? records
            .map(
              (row) => `
        <article class="step-card">
          <h4>${escapeHtml(row.createTime || "—")} · ${turnStatusTag(row.status)} · ${escapeHtml(formatDuration(row.durationMs))}</h4>
          <p class="step-summary">${escapeHtml(row.userText || "—")}</p>
          <div class="step-actions">
            <button class="btn-link" data-turn-detail="${row.id}">查看本轮详情</button>
          </div>
        </article>`,
            )
            .join("")
        : '<p class="empty">该会话暂无轮次</p>'}
    `;
    openDrawer("同会话记录", html, null, {
      hideSave: true,
      wide: true,
    });
    qs("drawer-body")
      .querySelectorAll("[data-turn-detail]")
      .forEach((btn) => btn.addEventListener("click", () => openTurnDetail(btn.dataset.turnDetail)));
  }

  async function loadTurn() {
    const tbody = qs("turn-tbody");
    tbody.innerHTML = emptyRow(8, "加载中…");
    const params = new URLSearchParams({
      current: String(state.turn.current),
      size: String(state.turn.size),
    });
    if (state.turn.keyword) {
      params.set("userText", state.turn.keyword);
    }
    try {
      const { records, total } = pageRecords(await request(`/ai/ai-agent-turn/page?${params}`));
      state.turn.total = total;
      if (!records.length) {
        tbody.innerHTML = emptyRow(8, "暂无数据。群里问机器人后会出现在这里。");
      } else {
        tbody.innerHTML = records
          .map((row) => `<tr>
            <td class="col-turn-text" title="${escapeHtml(row.userText || "")}"><span class="ellipsis">${escapeHtml(row.userText || "—")}</span></td>
            <td class="mono" title="${escapeHtml(row.conversationId || "")}">${escapeHtml(row.conversationId || "—")}</td>
            <td>${escapeHtml(row.botCode || row.botId || "—")}</td>
            <td>${escapeHtml(row.toolCallCount ?? 0)}</td>
            <td>${escapeHtml(formatDuration(row.durationMs))}</td>
            <td>${turnStatusTag(row.status)}</td>
            <td>${escapeHtml(row.createTime || "—")}</td>
            <td class="col-op">
              <button class="btn-link" data-view="${row.id}">详情</button>
              <button class="btn-link" data-session="${escapeHtml(row.sessionId || "")}">同会话</button>
            </td>
          </tr>`)
          .join("");
        tbody.querySelectorAll("[data-view]").forEach((btn) => {
          btn.addEventListener("click", () => openTurnDetail(btn.dataset.view));
        });
        tbody.querySelectorAll("[data-session]").forEach((btn) => {
          btn.addEventListener("click", () => listTurnsBySession(btn.dataset.session));
        });
      }
      renderPager(qs("turn-pager"), state.turn, loadTurn);
    } catch (error) {
      tbody.innerHTML = emptyRow(8, error.message);
    }
  }

  async function findChangeByTurnNo(turnNo) {
    if (!turnNo) {
      return null;
    }
    const params = new URLSearchParams({ current: "1", size: "1", turnNo });
    const { records } = pageRecords(await request(`/ai/ai-code-change/page?${params}`));
    return records[0] || null;
  }

  async function openTurnDetail(id) {
    const detail = await request(`/ai/ai-agent-turn/${id}`);
    const steps = detail.steps || [];
    let change = null;
    try {
      change = await findChangeByTurnNo(detail.turnNo);
    } catch (error) {
      change = null;
    }
    const html = `
      <dl class="meta-grid">
        <dt>用户原话</dt><dd>${escapeHtml(detail.userText || "—")}</dd>
        <dt>对话 ID</dt><dd class="mono">${escapeHtml(detail.conversationId || "—")}</dd>
        <dt>Session ID</dt><dd class="mono">${escapeHtml(detail.sessionId || "—")} ${detail.sessionId ? `<button class="btn-link inline-link" data-session-detail="${escapeHtml(detail.sessionId)}">查看同会话</button>` : ""}</dd>
        <dt>轮次号</dt><dd class="mono">${escapeHtml(detail.turnNo || "—")}</dd>
        <dt>提问人</dt><dd>${escapeHtml(detail.userId || "—")}</dd>
        <dt>机器人</dt><dd>${escapeHtml(detail.botCode || detail.botId || "—")}</dd>
        <dt>状态</dt><dd>${turnStatusTag(detail.status)} · ${escapeHtml(formatDuration(detail.durationMs))} · 工具 ${escapeHtml(detail.toolCallCount ?? 0)} 次${Number(detail.hasImage) === 1 ? " · 带图" : ""}</dd>
        <dt>最终回复</dt><dd>${escapeHtml(detail.finalAnswer || detail.errorMessage || "—")}</dd>
      </dl>
      <h3 class="section-title">工具时间线</h3>
      ${steps.length ? steps.map((step) => `
        <article class="step-card">
          <h4>#${escapeHtml(step.seq ?? "—")}
            · ${escapeHtml(toolLabel(step.toolCode))}
            · ${escapeHtml(step.toolName || "")}
            · ${escapeHtml(formatDuration(step.durationMs))}
            · ${Number(step.success) === 0 ? '<span class="tag tag-off">失败</span>' : '<span class="tag tag-ok">成功</span>'}
          </h4>
          <p class="step-summary">${escapeHtml(step.responseSummary || "—")}</p>
          <pre class="diff">${escapeHtml(step.requestJson || "{}")}</pre>
          <pre class="diff">${escapeHtml(step.responseJson || step.errorMessage || "（无返回）")}</pre>
        </article>
      `).join("") : '<p class="empty">本轮没有工具调用</p>'}
    `;
    openDrawer("对话详情", html, null, {
      hideSave: true,
      wide: true,
      extraLabel: "查看改代码",
      onExtra: change ? () => openChangeDetail(change.id) : null,
    });
    qs("drawer-body")
      .querySelector("[data-session-detail]")
      ?.addEventListener("click", () => listTurnsBySession(detail.sessionId));
  }

  function changeStatusTag(status) {
    if (Number(status) === 1) {
      return '<span class="tag tag-ok">已应用</span>';
    }
    if (Number(status) === 2) {
      return '<span class="tag tag-muted">部分成功</span>';
    }
    if (Number(status) === 3) {
      return '<span class="tag tag-off">失败</span>';
    }
    return '<span class="tag tag-muted">待应用</span>';
  }

  function patchStatusTag(status) {
    if (Number(status) === 1) {
      return '<span class="tag tag-ok">已应用</span>';
    }
    if (Number(status) === 2) {
      return '<span class="tag tag-off">冲突</span>';
    }
    if (Number(status) === 3) {
      return '<span class="tag tag-off">失败</span>';
    }
    return '<span class="tag tag-muted">待应用</span>';
  }

  function canApplyChange(row) {
    return row?.writeMode === "DIFF_FILE" && Number(row.status) !== 1;
  }

  async function loadChange() {
    const tbody = qs("change-tbody");
    tbody.innerHTML = emptyRow(8, "加载中…");
    const params = new URLSearchParams({
      current: String(state.change.current),
      size: String(state.change.size),
    });
    if (state.change.keyword) {
      params.set("title", state.change.keyword);
    }
    try {
      const { records, total } = pageRecords(await request(`/ai/ai-code-change/page?${params}`));
      state.change.total = total;
      if (!records.length) {
        tbody.innerHTML = emptyRow(8, "暂无数据。群里让机器人改代码后会出现在这里。");
      } else {
        tbody.innerHTML = records
          .map((row) => `<tr>
            <td title="${escapeHtml(row.requestText || "")}">${escapeHtml(row.title || "—")}</td>
            <td class="mono" title="${escapeHtml(row.conversationId || "")}">${escapeHtml(row.conversationId || "—")}</td>
            <td>${escapeHtml(row.botCode || row.botId || "—")}</td>
            <td>${escapeHtml(row.patchCount ?? 0)}</td>
            <td>${escapeHtml(row.writeMode || "—")}</td>
            <td>${changeStatusTag(row.status)}</td>
            <td>${escapeHtml(row.createTime || "—")}</td>
            <td class="col-op">
              <button class="btn-link" data-view="${row.id}">详情</button>
              ${canApplyChange(row) ? `<button class="btn-link" data-apply="${row.id}">Apply</button>` : ""}
            </td>
          </tr>`)
          .join("");
        tbody.querySelectorAll("[data-view]").forEach((btn) => {
          btn.addEventListener("click", () => openChangeDetail(btn.dataset.view));
        });
        tbody.querySelectorAll("[data-apply]").forEach((btn) => {
          btn.addEventListener("click", () => applyChange(btn.dataset.apply));
        });
      }
      renderPager(qs("change-pager"), state.change, loadChange);
    } catch (error) {
      tbody.innerHTML = emptyRow(8, error.message);
    }
  }

  async function openChangeDetail(id) {
    const detail = await request(`/ai/ai-code-change/${id}`);
    const patches = detail.patches || [];
    const html = `
      <dl class="meta-grid">
        <dt>功能点</dt><dd>${escapeHtml(detail.title || "—")}</dd>
        <dt>对话 ID</dt><dd>${escapeHtml(detail.conversationId || "—")}</dd>
        <dt>轮次号</dt><dd>${escapeHtml(detail.turnNo || "—")}</dd>
        <dt>提问人</dt><dd>${escapeHtml(detail.userId || "—")}</dd>
        <dt>机器人</dt><dd>${escapeHtml(detail.botCode || detail.botId || "—")}</dd>
        <dt>模式</dt><dd>${escapeHtml(detail.writeMode || "—")} ${changeStatusTag(detail.status)}</dd>
        <dt>原话</dt><dd>${escapeHtml(detail.requestText || "—")}</dd>
      </dl>
      ${(patches.length ? patches : []).map((patch) => `
        <article class="patch-card">
          <h4>${escapeHtml(patch.sourcePath)}
            · +${patch.addedLines || 0}/-${patch.removedLines || 0}
            · ${patchStatusTag(patch.status)}
            ${patch.patchFile ? ` · ${escapeHtml(patch.patchFile)}` : ""}
          </h4>
          <pre class="diff">${escapeHtml(patch.unifiedDiff || "（无 diff）")}</pre>
        </article>
      `).join("") || '<p class="empty">没有 patch 文件</p>'}
    `;
    openDrawer("改动详情", html, null, {
      hideSave: true,
      wide: true,
      extraLabel: "应用这次修改",
      onExtra: canApplyChange(detail) ? () => applyChange(detail.id) : null,
    });
  }

  function applyChange(id) {
    confirmAction(
      "应用这次修改",
      "将把本功能点下尚未应用的 patch 写入沙箱源文件。同一文件以最后一次为准；若源文件已变动会标为冲突。",
      "应用",
      async () => {
        const result = await request(`/ai/ai-code-change/${id}/apply`, { method: "POST" });
        toast(result?.message || "已处理");
        closeDrawer();
        await loadChange();
      },
      false,
    );
  }

  function showPage(name) {
    state.page = name;
    qs("page-prompt").classList.toggle("is-hidden", name !== "prompt");
    qs("page-bot").classList.toggle("is-hidden", name !== "bot");
    qs("page-change").classList.toggle("is-hidden", name !== "change");
    qs("page-turn").classList.toggle("is-hidden", name !== "turn");
    document.querySelectorAll(".menu-item").forEach((item) => {
      item.classList.toggle("is-active", item.dataset.page === name);
    });
    const titles = { bot: "机器人配置", change: "改代码历史", prompt: "Prompt 模板", turn: "对话日志" };
    qs("header-title").textContent = titles[name] || "Prompt 模板";
    if (name === "bot") {
      loadBot();
    } else if (name === "change") {
      loadChange();
    } else if (name === "turn") {
      loadTurn();
    } else {
      loadPrompt();
    }
  }

  function route() {
    const hash = (location.hash || "#/prompt").replace("#/", "");
    showPage(hash === "bot" || hash === "change" || hash === "turn" ? hash : "prompt");
  }

  qs("prompt-search").addEventListener("click", () => {
    state.prompt.keyword = formValue("prompt-keyword");
    state.prompt.current = 1;
    loadPrompt();
  });
  qs("prompt-keyword").addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      qs("prompt-search").click();
    }
  });
  qs("prompt-create").addEventListener("click", () => openPromptForm(null));

  qs("bot-search").addEventListener("click", () => {
    state.bot.keyword = formValue("bot-keyword");
    state.bot.current = 1;
    loadBot();
  });
  qs("bot-keyword").addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      qs("bot-search").click();
    }
  });
  qs("bot-create").addEventListener("click", () => openBotForm(null));

  qs("change-search").addEventListener("click", () => {
    state.change.keyword = formValue("change-keyword");
    state.change.current = 1;
    loadChange();
  });
  qs("change-keyword").addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      qs("change-search").click();
    }
  });

  qs("turn-search").addEventListener("click", () => {
    state.turn.keyword = formValue("turn-keyword");
    state.turn.current = 1;
    loadTurn();
  });
  qs("turn-keyword").addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      qs("turn-search").click();
    }
  });

  qs("drawer-close").addEventListener("click", closeDrawer);
  qs("drawer-cancel").addEventListener("click", closeDrawer);
  qs("drawer-mask").addEventListener("click", closeDrawer);
  qs("drawer-save").addEventListener("click", async () => {
    if (!state.drawer?.onSave) {
      return;
    }
    try {
      await state.drawer.onSave();
      closeDrawer();
    } catch (error) {
      toast(error.message, true);
    }
  });
  qs("drawer-extra").addEventListener("click", async () => {
    if (!state.drawer?.onExtra) {
      return;
    }
    try {
      await state.drawer.onExtra();
    } catch (error) {
      toast(error.message, true);
    }
  });

  qs("confirm-cancel").addEventListener("click", closeConfirm);
  qs("confirm-ok").addEventListener("click", async () => {
    const job = state.confirm?.onOk;
    closeConfirm();
    if (!job) {
      return;
    }
    try {
      await job();
    } catch (error) {
      toast(error.message, true);
    }
  });

  window.addEventListener("hashchange", route);
  route();
})();

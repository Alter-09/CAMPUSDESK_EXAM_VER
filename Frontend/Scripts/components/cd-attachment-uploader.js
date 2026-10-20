import {
  getTicketAttachments,
  uploadAttachmentMetadata,
  deleteAttachment
} from "../services/attachment.service.js";

// [Nueva Funcionalidad: U-05/T-16 - Gestor y Subida de Archivos Adjuntos] - Permite adjuntar y visualizar evidencias en tickets - Conecta con: attachment.service.js y <cd-attachment-uploader>
class CdAttachmentUploader extends HTMLElement {
  constructor() {
    super();
    this.attachShadow({ mode: "open" });
    this.ticketId = null;
    this.attachments = [];
    this.isUploading = false;
  }

  static get observedAttributes() {
    return ["ticket-id", "readonly"];
  }

  attributeChangedCallback(name, oldValue, newValue) {
    if (name === "ticket-id" && newValue) {
      this.ticketId = newValue;
      this.loadAttachments();
    }
    if (name === "readonly") {
      this.render();
    }
  }

  connectedCallback() {
    this.render();
    if (this.ticketId) {
      this.loadAttachments();
    }
  }

  async loadAttachments() {
    if (!this.ticketId) return;
    try {
      this.attachments = await getTicketAttachments(this.ticketId);
      this.render();
    } catch (err) {
      console.warn("cd-attachment-uploader: Could not load attachments", err);
    }
  }

  formatBytes(bytes, decimals = 1) {
    if (!bytes || bytes === 0) return "0 B";
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ["B", "KB", "MB", "GB"];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + " " + sizes[i];
  }

  async handleFiles(files) {
    if (!this.ticketId) {
      alert("Por favor selecciona o crea un tiquete antes de adjuntar archivos.");
      return;
    }
    const maxSizeBytes = 10 * 1024 * 1024; // 10MB
    for (const file of files) {
      if (file.size > maxSizeBytes) {
        alert(`El archivo ${file.name} supera el límite de 10 MB.`);
        continue;
      }
      this.isUploading = true;
      this.render();
      try {
        const metadata = {
          fileName: file.name,
          contentType: file.type || "application/octet-stream",
          fileSizeBytes: file.size,
          storagePath: `/uploads/tickets/${this.ticketId}/${encodeURIComponent(file.name)}`
        };
        await uploadAttachmentMetadata(this.ticketId, metadata);
        this.dispatchEvent(new CustomEvent("attachment-uploaded", {
          bubbles: true,
          composed: true,
          detail: { fileName: file.name, ticketId: this.ticketId }
        }));
      } catch (err) {
        alert(`Error al subir ${file.name}: ${err.message || err}`);
      }
    }
    this.isUploading = false;
    await this.loadAttachments();
  }

  async removeFile(id) {
    if (!confirm("¿Deseas eliminar este archivo adjunto?")) return;
    try {
      await deleteAttachment(id);
      this.attachments = this.attachments.filter(a => a.id !== id);
      this.render();
      this.dispatchEvent(new CustomEvent("attachment-deleted", {
        bubbles: true,
        composed: true,
        detail: { id, ticketId: this.ticketId }
      }));
    } catch (err) {
      alert("Error eliminando archivo: " + (err.message || err));
    }
  }

  getFileIcon(contentType) {
    if (!contentType) return "📄";
    if (contentType.includes("image")) return "🖼️";
    if (contentType.includes("pdf")) return "📕";
    if (contentType.includes("sheet") || contentType.includes("csv")) return "📊";
    if (contentType.includes("zip") || contentType.includes("tar") || contentType.includes("compressed")) return "📦";
    return "📄";
  }

  render() {
    const isReadOnly = this.getAttribute("readonly") === "true";

    this.shadowRoot.innerHTML = `
      <style>
        :host {
          display: block;
          font-family: inherit;
          color: #1e293b;
        }
        .container {
          background: #ffffff;
          border-radius: 12px;
          border: 1px solid #e2e8f0;
          padding: 16px;
        }
        .header {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 12px;
        }
        .title {
          font-size: 0.95rem;
          font-weight: 700;
          margin: 0;
          color: #0f172a;
        }
        .count-badge {
          background: #f1f5f9;
          color: #475569;
          font-size: 0.75rem;
          padding: 2px 8px;
          border-radius: 999px;
          font-weight: 600;
        }
        .dropzone {
          border: 2px dashed #cbd5e1;
          border-radius: 10px;
          padding: 20px;
          text-align: center;
          background: #f8fafc;
          cursor: pointer;
          transition: border-color 0.15s, background 0.15s;
        }
        .dropzone:hover, .dropzone.dragover {
          border-color: #3b82f6;
          background: #eff6ff;
        }
        .dropzone-icon {
          font-size: 1.75rem;
          margin-bottom: 4px;
        }
        .dropzone-text {
          font-size: 0.85rem;
          color: #475569;
          margin: 0;
        }
        .dropzone-hint {
          font-size: 0.75rem;
          color: #94a3b8;
          margin-top: 4px;
        }
        .file-list {
          margin-top: 14px;
          display: flex;
          flex-direction: column;
          gap: 8px;
        }
        .file-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 8px 12px;
          background: #f8fafc;
          border: 1px solid #e2e8f0;
          border-radius: 8px;
          font-size: 0.85rem;
        }
        .file-info {
          display: flex;
          align-items: center;
          gap: 8px;
          overflow: hidden;
        }
        .file-name {
          font-weight: 500;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
          max-width: 220px;
        }
        .file-size {
          font-size: 0.75rem;
          color: #64748b;
        }
        .btn-delete {
          background: none;
          border: none;
          color: #ef4444;
          cursor: pointer;
          font-size: 0.9rem;
          padding: 2px 6px;
          border-radius: 4px;
        }
        .btn-delete:hover {
          background: #fee2e2;
        }
        input[type="file"] {
          display: none;
        }
        .spinner {
          display: inline-block;
          font-size: 0.85rem;
          color: #2563eb;
          margin-top: 8px;
        }
      </style>

      <div class="container">
        <div class="header">
          <h4 class="title">📎 Archivos y Evidencias</h4>
          <span class="count-badge">${this.attachments.length} archivos</span>
        </div>

        ${!isReadOnly ? `
          <div class="dropzone" id="dropzone">
            <div class="dropzone-icon">📁</div>
            <p class="dropzone-text"><b>Haz clic para subir</b> o arrastra archivos aquí</p>
            <p class="dropzone-hint">PNG, JPG, PDF, DOCX o ZIP hasta 10MB</p>
            <input type="file" id="file-input" multiple />
          </div>
        ` : ""}

        ${this.isUploading ? `<div class="spinner">⏳ Subiendo archivos...</div>` : ""}

        <div class="file-list">
          ${this.attachments.map(att => `
            <div class="file-item">
              <div class="file-info">
                <span>${this.getFileIcon(att.contentType)}</span>
                <span class="file-name" title="${att.fileName}">${att.fileName}</span>
                <span class="file-size">(${this.formatBytes(att.fileSizeBytes)})</span>
              </div>
              ${!isReadOnly ? `
                <button class="btn-delete" data-id="${att.id}" title="Eliminar archivo">🗑️</button>
              ` : ""}
            </div>
          `).join("")}
        </div>
      </div>
    `;

    this.attachEvents();
  }

  attachEvents() {
    const dropzone = this.shadowRoot.getElementById("dropzone");
    const fileInput = this.shadowRoot.getElementById("file-input");

    if (dropzone && fileInput) {
      dropzone.addEventListener("click", () => fileInput.click());
      fileInput.addEventListener("change", (e) => {
        if (e.target.files?.length) {
          this.handleFiles(Array.from(e.target.files));
        }
      });

      dropzone.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropzone.classList.add("dragover");
      });

      dropzone.addEventListener("dragleave", () => {
        dropzone.classList.remove("dragover");
      });

      dropzone.addEventListener("drop", (e) => {
        e.preventDefault();
        dropzone.classList.remove("dragover");
        if (e.dataTransfer?.files?.length) {
          this.handleFiles(Array.from(e.dataTransfer.files));
        }
      });
    }

    this.shadowRoot.querySelectorAll(".btn-delete").forEach(btn => {
      btn.addEventListener("click", (e) => {
        e.stopPropagation();
        const id = btn.getAttribute("data-id");
        if (id) this.removeFile(id);
      });
    });
  }
}

if (!customElements.get("cd-attachment-uploader")) {
  customElements.define("cd-attachment-uploader", CdAttachmentUploader);
}

export default CdAttachmentUploader;

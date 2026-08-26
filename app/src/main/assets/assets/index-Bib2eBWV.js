const __parts = 52;
const __texts = await Promise.all(
  Array.from({length: __parts}, (_, i) =>
    fetch("/assets/index-Bib2eBWV.part" + String(i).padStart(3,"0") + ".txt").then(r => {
      if (!r.ok) throw new Error("bib chunk " + i + " " + r.status);
      return r.text();
    })
  )
);
let __src = __texts.join("");
__src = __src.replace(/from(["'])\.\//g, "from$1/assets/");
__src = __src.replace(/import\((["'])\.\//g, "import($1/assets/");
const __url = URL.createObjectURL(new Blob([__src], {type: "text/javascript"}));
const __m = await import(__url);
export const A = __m.A;
export const j = __m.j;
export const m = __m.m;
export const r = __m.r;
export const u = __m.u;

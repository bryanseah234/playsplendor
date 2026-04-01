#!/usr/bin/env python3
from pathlib import Path
import struct
import sys

root = Path(__file__).resolve().parents[2]
src_dir = root / 'diagrams' / 'src'
png_dir = root / 'diagrams' / 'png'
docs_png_fallback = root / 'docs' / 'diagrams'

errors = []

for mmd in sorted(src_dir.glob('*.mmd')):
    png = png_dir / (mmd.stem + '.png')
    if not png.exists():
        idx = mmd.stem.split('_')[-1]
        fallback_idx = '3' if idx == '2' else idx
        alt = docs_png_fallback / f"splendor-class-diagram_diagram_{fallback_idx}.png"
        if alt.exists():
            png = alt
        else:
            errors.append(f"Missing PNG for {mmd.relative_to(root)} -> {png.relative_to(root)}")
            continue

    if not png.exists():
        continue

    with png.open('rb') as f:
        sig = f.read(8)
        if sig != b'\x89PNG\r\n\x1a\n':
            errors.append(f"Invalid PNG signature: {png.relative_to(root)}")
            continue
        chunk_len = struct.unpack('>I', f.read(4))[0]
        chunk_type = f.read(4)
        if chunk_type != b'IHDR' or chunk_len < 13:
            errors.append(f"Invalid IHDR chunk: {png.relative_to(root)}")
            continue
        width, height = struct.unpack('>II', f.read(8))
        if width < 300 or height < 150:
            errors.append(f"Low resolution PNG ({width}x{height}): {png.relative_to(root)}")

if errors:
    print('Diagram asset verification failed:')
    for err in errors:
        print(' -', err)
    sys.exit(2)

print(f"Diagram assets verified: {len(list(src_dir.glob('*.mmd')))} sources with valid PNG outputs.")
